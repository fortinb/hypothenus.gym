package com.isoceles.hypothenus.gym.domain.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.isoceles.hypothenus.gym.domain.model.Gym;

public interface IGymRepository extends PagingAndSortingRepository<Gym, String>, CrudRepository<Gym, String>{
	Optional<Gym> findByGymIdAndIsDeleted(String gymId, boolean isDeleted);
	Page<Gym> findAllByAndIsDeleted(TextCriteria criteria, Pageable pageable, boolean isDeleted);
	Page<Gym> findAllByIsDeleted(Pageable pageable, boolean isDeleted);
}
