package com.springboot.controller;

import com.springboot.pojo.Dept;
import com.springboot.pojo.Result;
import com.springboot.service.DeptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequestMapping("api/depts")
@RestController
public class DeptController {

    @Autowired
    private DeptService deptService;

    @GetMapping
    public Result list() {
        //System.out.println("查询全部部门数据");
        log.info("查询全部部门数据");
        List<Dept> deptList = deptService.findAll();
        return Result.success(deptList);
    }

    @DeleteMapping
    //接收请求参数
    public Result delete(Integer id) {
        //System.out.println("根据ID删除部门数据:"+id);
        log.info("根据ID删除部门数据:{}",id);
        deptService.deleteById(id);
        return Result.success();
    }

    @PostMapping
    //接收请求参数，json格式
    public Result add(@RequestBody Dept dept) {
        //System.out.println("新增部门数据："+dept);
        log.info("新增部门数据：{}",dept);
        deptService.add(dept);
        return Result.success();
    }

    @GetMapping("/{id}")
    //接收路径参数
    public Result getInfo(@PathVariable Integer id) {
        //System.out.println("查询部门数据："+id);
        log.info("根据ID查询部门数据：{}",id);
        Dept dept = deptService.getById(id);
        return Result.success(dept);
    }

    @PutMapping
    public Result update(@RequestBody Dept dept) {
        //System.out.println("修改部门数据："+dept);
        log.info("修改部门数据：{}",dept);
        deptService.update(dept);
        return Result.success();
    }

}
