package com.onedayoffer.taskdistribution.services;

import com.onedayoffer.taskdistribution.ConfigurationClass;
import com.onedayoffer.taskdistribution.DTO.EmployeeDTO;
import com.onedayoffer.taskdistribution.DTO.TaskDTO;
import com.onedayoffer.taskdistribution.DTO.TaskStatus;
import com.onedayoffer.taskdistribution.repositories.EmployeeRepository;
import com.onedayoffer.taskdistribution.repositories.TaskRepository;
import com.onedayoffer.taskdistribution.repositories.entities.Employee;
import com.onedayoffer.taskdistribution.repositories.entities.Task;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final TaskRepository taskRepository;
    private final ModelMapper modelMapper;

    public List<EmployeeDTO> getEmployees(@Nullable String sortDirection) {
        Optional<Sort.Direction> direction = Sort.Direction.fromOptionalString(sortDirection);
        List<Employee> employees;
        if (direction.isPresent()) {
            employees = employeeRepository.findAllAndSort(Sort.by(direction.get(), "fio"));
        } else {
            employees = employeeRepository.findAll();
        }
        log.info("Employees are found! Size: {}", employees.size());
        Type listType = new TypeToken<List<EmployeeDTO>>() {}.getType();
        return modelMapper.map(employees, listType);
    }

    @Transactional
    public EmployeeDTO getOneEmployee(Integer id) {
        if (Objects.isNull(id) || id < 0) {
            throw new IllegalArgumentException("Employee id cannot be null or a negative number!");
        }
        var employees = employeeRepository.findById(id);
        Type listType = new TypeToken<List<EmployeeDTO>>() {}.getType();
        return modelMapper.map(employees, listType);
    }

    public List<TaskDTO> getTasksByEmployeeId(Integer id) {
        if (Objects.isNull(id) || id < 0) {
            throw new IllegalArgumentException("Task id cannot be null or a negative number!");
        }
        List<Task> tasks = taskRepository.findAllTaskByEmployeeId(id);
        Type listType = new TypeToken<List<TaskDTO>>() {}.getType();
        return modelMapper.map(tasks, listType);
    }

    @Transactional
    public void changeTaskStatus(Integer taskId, TaskStatus status) {
        if (Objects.isNull(taskId)
                || taskId < 0
                || Objects.isNull(status)) {
            throw new IllegalArgumentException("Task id cannot be null or a negative number!");
        }
        Optional<Task> task = taskRepository.findById(taskId);
        task.ifPresent(t -> t.setStatus(status));

    }

    @Transactional
    public void postNewTask(Integer employeeId, TaskDTO newTask) {
        Type taskType = new TypeToken<TaskDTO>() {}.getType();
        Task taskEntity = modelMapper.map(newTask, taskType);
        taskRepository.saveAndFlush(taskEntity);
    }
}
