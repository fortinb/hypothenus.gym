package com.isoceles.hypothenus.gym.domain.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.isoceles.hypothenus.gym.domain.model.Gym;

public interface IGymRepository extends PagingAndSortingRepository<Gym, String>, CrudRepository<Gym, String>{

}
