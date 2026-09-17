package com.ncs.fault.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ncs.common.api.Result;
import com.ncs.fault.entity.Fault;
import com.ncs.fault.mapper.FaultMapper;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

@RestController
@RequestMapping("/api/admin/fault")
public class AdminFaultController {

    private final FaultMapper faultMapper;

    public AdminFaultController(FaultMapper faultMapper) {
        this.faultMapper = faultMapper;
    }

    @GetMapping("/list")
    public Result<Page<Fault>> list(@RequestParam(defaultValue = "1") Integer page,
                                    @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.ok(faultMapper.selectPage(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<Fault>().orderByDesc(Fault::getFaultTime)));
    }

    @PostMapping
    public Result<Fault> create(@RequestBody Fault fault) {
        fault.setId(null);
        if (fault.getFaultTime() == null) {
            fault.setFaultTime(LocalDateTime.now());
        }
        if (fault.getStatus() == null) {
            fault.setStatus(Fault.STATUS_PENDING);
        }
        faultMapper.insert(fault);
        return Result.ok(fault);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Fault fault) {
        fault.setId(id);
        faultMapper.updateById(fault);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        faultMapper.deleteById(id);
        return Result.ok();
    }
}
