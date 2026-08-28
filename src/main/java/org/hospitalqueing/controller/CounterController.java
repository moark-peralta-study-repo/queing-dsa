package org.hospitalqueing.controller;

import java.util.List;

import org.hospitalqueing.model.Counter;
import org.hospitalqueing.service.CounterService;

public class CounterController {
  private final CounterService counterService;

  public CounterController(CounterService counterService) {
    this.counterService = counterService;
  }

  public void createCounter(Counter counter) {
    counterService.createCounter(counter);
  }

  public Counter getCounter(int counterId) {
    return counterService.getCounterById(counterId);
  }

  public List<Counter> getAllCounters() {
    return counterService.getAllCounters();
  }

  public List<Counter> getCountersByDepartment(int departmentId) {
    return counterService.getCountersByDepartment(departmentId);
  }

  public void updateCounter(Counter counter) {
    counterService.updateCounter(counter);
  }

  public void deleteCounter(int counterId) {
    counterService.deleteCounter(counterId);
  }
}
