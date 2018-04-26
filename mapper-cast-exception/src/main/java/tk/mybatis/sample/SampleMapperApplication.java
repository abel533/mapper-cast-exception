/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 abel533@gmail.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package tk.mybatis.sample;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.sample.domain.Country;
import tk.mybatis.sample.mapper.CountryMapper;
import tk.mybatis.spring.annotation.MapperScan;

@MapperScan(basePackages = "tk.mybatis.sample.mapper")
@SpringBootApplication
@RestController
@RequestMapping
public class SampleMapperApplication {

    @Autowired
    private CountryMapper countryMapper;

    public static void main(String[] args) {
        SpringApplication.run(SampleMapperApplication.class, args);
    }

    @RequestMapping("/{id}")
    @ResponseBody
    public Country byId(@PathVariable("id") Long id) {
        Thread.currentThread().getThreadGroup()
        //为什么这里的 Country 是 RestartClassLoader 中的呢？
        //RestartLauncher 线程设置的 RestartClassLoader，然后重新调用这里的 main(String[] args) 方法
        //通过某个线程创建子线程时，这些线程会和父线程使用相同的 ClassLoader
        Object obj = countryMapper.selectByPrimaryKey(id);
        Country country = countryMapper.selectByPrimaryKey(id);
        System.out.println(CountryMapper.class.getClassLoader());
        System.out.println(countryMapper.getClass().getClassLoader());
        System.out.println(country.getClass().getClassLoader());
        return country;
    }
}
