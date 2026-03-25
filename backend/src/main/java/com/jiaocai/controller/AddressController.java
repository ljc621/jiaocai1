package com.jiaocai.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jiaocai.common.Result;
import com.jiaocai.entity.Address;
import com.jiaocai.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@CrossOrigin
public class AddressController {

    @Autowired
    private AddressService addressService;

    @GetMapping
    public Result<List<Address>> list(@RequestParam String userId) {
        LambdaQueryWrapper<Address> query = new LambdaQueryWrapper<>();
        query.eq(Address::getUserId, userId);
        return Result.success(addressService.list(query));
    }

    @GetMapping("/{id}")
    public Result<Address> getById(@PathVariable String id) {
        return Result.success(addressService.getById(id));
    }

    @PostMapping
    public Result<Address> create(@RequestBody Address address) {
        if (address.getIsDefault() != null && address.getIsDefault()) {
            // Clear other defaults
            clearDefaults(address.getUserId());
        }
        addressService.save(address);
        return Result.success(address);
    }

    @PutMapping
    public Result<Address> update(@RequestBody Address address) {
        if (address.getIsDefault() != null && address.getIsDefault()) {
            clearDefaults(address.getUserId());
        }
        addressService.updateById(address);
        return Result.success(address);
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable String id) {
        addressService.removeById(id);
        return Result.success("Address deleted");
    }

    private void clearDefaults(String userId) {
        LambdaQueryWrapper<Address> updateWrapper = new LambdaQueryWrapper<>();
        updateWrapper.eq(Address::getUserId, userId);
        Address updateEntity = new Address();
        updateEntity.setIsDefault(false);
        addressService.update(updateEntity, updateWrapper);
    }
}
