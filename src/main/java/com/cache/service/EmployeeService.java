package com.cache.service;

import com.cache.entity.Employee;
import com.cache.repo.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class EmployeeService {

//    @Autowired
    EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public Employee saveEmployee(Employee employee) {
        System.out.println("Save the record");
        return employeeRepository.save(employee);
    }

//    @Cacheable(value = "employee", key = "#id")
    public Employee getEmployeeById(int id){
        try{
            System.out.println("Get the record with id : " + id);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
            String formattedDate = LocalDateTime.now().format(formatter);
            System.out.println("Formatted Timestamp: " + formattedDate);
            return employeeRepository.findById(id).orElse(null);
        }catch (Exception e){
            System.out.println("Exception: " + e);
            throw e;
        }


    }

    @CachePut(value = "employee", key = "#employee.id")
    public Employee updateEmployee(Employee employee) {
        System.out.println("Update the record with id : " + employee.getId());
        return employeeRepository.save(employee);
    }

    @CacheEvict(value = "employee", key = "#id")
    public void deleteEmployee(int id) {
        System.out.println("Delete the record with id : " + id);
        employeeRepository.deleteById(id);
    }
}
