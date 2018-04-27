### Spring Boot Devtools Cannot cast x.y.Z to x.y.Z

经过挺长时间的测试和分析，才找到原因，并且有了本项目。

### 为什么会出现 `Cannot cast x.y.Z to x.y.Z`？

首先 Devtools 是应用于开发时的，也就是在 IDE 中运行时，当使用 jar 方式运行时，就会自动禁用 Devtools 工具。在 IDE 中运行时，Devtools 会通过独立的类加载器来加载会发生变化的资源，通常是 IDE 各个模块的 classes 目录。对于通过依赖导入的 jar 包是不会自动更新的。

这两部分内容分别由 RestartClassLoader 和 AppClassLoader 加载的，并且前者的 parent 是后者，也就是说 AppClassLoader 加载的类可以在 RestartClassLoader 加载的类中使用，但是 RestartClassLoader 加载的类不能在 AppClassLoader 中使用。

**这里有下面三个项目：**

- mapper-cast-exception（包含启动类，依赖 mapper-cast-exception-mapper 和 mapper-cast-exception-model）
- mapper-cast-exception-model（包含实体类）
- mapper-cast-exception-mapper（包含 Mapper 接口，依赖 mapper-cast-exception-model）

假如这3个项目都已经在本地或者私服打包。

在 IDE 中只引入 mapper-cast-exception 和 mapper-cast-exception-model 项目。

此时使用 Devtools 时，RestartClassLoader 会加载并检测这两个项目的变化，mapper 项目会使用 AppClassLoader 加载，AppClassLoader 启动时也会加载前两个项目中的类。

>RestartClassLoader 实现中，是把 AppClassLoader 已经加载的类中，所有以 `file:` 开头和 `/` 结尾的类路径（非 `.jar` 文件，也就是所有类路径中的目录）加载到了 RestartClassLoader 中，所以 AppClassLoader 实际上是全的。后续通过 RestartClassLoader 加载时，会先判断 RestartClassLoader 中是否包含类，这里优先使用 RestartClassLoader 提供的类，如果不存在才会通过 parent（AppClassLoader）查找。

在下面的方法中，当通过 AppClassLoader 加载的 mapper 调用返回 model 时，该 model 类是由 AppClassLoader 加载的。而下面代码中的 Country 是 RestartClassLoader 加载的，由于是不同的 ClassLoader，因此就会抛出异常。

```java
@RequestMapping("/{id}")
@ResponseBody
public Country byId(@PathVariable("id") Long id) {
  //这样不报错
  Object obj = countryMapper.selectByPrimaryKey(id);
  //这样会报错，直接 return 也报错
  Country country = countryMapper.selectByPrimaryKey(id);
  return country;
}
```

**有些人可能会问，为什么 Country 是 RestartClassLoader 加载的？**

因为 Country 是在当前线程加载的，而加载类的 ClassLoader 默认就是当前线程的 ContextClassLoader。

Devtools 在启动时（你运行的 `main` 方法），悄悄的创建了一个新的 `restartedMain` 线程，然后把你启动的 `main` 线程终止了。新的 `restartedMain` 线程使用的就是 RestartClassLoader，后续通过该线程创建的其他线程也都是这个类加载器。因此执行上面代码的方法时，Country 就是 RestartClassLoader 加载的。

到这里就应该明白了，实际上这个问题产生的原因和通用 Mapper 没有关系，在通用 Mapper 中增加的配置也没有用。

**还有一个问题，为什么只往 IDE 加载两个项目，而不是全部项目呢？**

如果你参与的项目有几十个模块，几十个模块之间可能存在类似的依赖关系，在工作时，往往只会把需要用到或者修改的项目导入到 IDE 中，因此就出现了这个问题。如果你真遇到这个问题，最直接的方法就是禁用 Devtools 工具。