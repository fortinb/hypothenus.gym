package com.isoceles.hypothenus.gym.domain.services;

import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.isoceles.hypothenus.gym.domain.exception.DomainException;
import com.isoceles.hypothenus.gym.domain.model.GymSearchResult;
import com.isoceles.hypothenus.gym.domain.model.aggregate.Gym;
import com.isoceles.hypothenus.gym.domain.repository.GymRepository;

@Service
public class GymService {

	private GymRepository gymRepository;

	public GymService(GymRepository gymRepository) {
		this.gymRepository = gymRepository;
	}

	public Gym create(Gym gym) throws DomainException {
		return gymRepository.save(gym);
	}

	public Gym update(Gym gym) throws DomainException {
		Gym oldGym = this.findByGymId(gym.getGymId());

		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setSkipNullEnabled(false);
		
		PropertyMap<Gym, Gym> gymPropertyMap = new PropertyMap<Gym, Gym>()
	    {
	        protected void configure()
	        {
	            skip().setId(null);
	        }
	    };
	    
		//mapper.addMappings(new GymPropertyMap());
		mapper.addMappings(gymPropertyMap);
		mapper.map(gym, oldGym);

		return gymRepository.save(oldGym);
	}

	public Gym patch(Gym gym) throws DomainException {
		Gym oldGym = this.findByGymId(gym.getGymId());

		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setSkipNullEnabled(true);
		
		PropertyMap<Gym, Gym> gymPropertyMap = new PropertyMap<Gym, Gym>()
	    {
	        protected void configure()
	        {
	            skip().setId(null);
	        }
	    };
		mapper.addMappings(gymPropertyMap);

		mapper.map(gym, oldGym);

		return gymRepository.save(oldGym);
	}

	public void delete(String gymId) throws DomainException {
		Gym oldGym = this.findByGymId(gymId);
		oldGym.setDeleted(true);

		gymRepository.save(oldGym);
	}

	public Gym findByGymId(String id) throws DomainException {
		Optional<Gym> entity = gymRepository.findByGymIdAndIsDeletedIsFalse(id);
		if (entity.isEmpty()) {
			throw new DomainException(DomainException.GYM_NOT_FOUND, "Gym not found");
		}

		return entity.get();
	}

	public List<GymSearchResult> search(String criteria) throws DomainException {
		return gymRepository.searchAutocomplete(criteria);
	}

	public Page<Gym> list(int page, int pageSize) throws DomainException {
		return gymRepository.findAllByIsDeletedIsFalse(PageRequest.of(page, pageSize, Sort.Direction.ASC, "name"));
	}
}
