package com.isoceles.hypothenus.gym.domain.repository;

import java.util.List;

import com.isoceles.hypothenus.gym.domain.model.GymSearchResult;

public interface GymQueries {

	List<GymSearchResult> searchAutocomplete(String criteria);
}
