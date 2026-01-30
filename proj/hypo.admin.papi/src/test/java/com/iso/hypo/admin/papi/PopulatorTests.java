package com.iso.hypo.admin.papi;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iso.hypo.domain.BrandBuilder;
import com.iso.hypo.domain.aggregate.Brand;
import com.iso.hypo.repositories.BrandRepository;
import com.iso.hypo.repositories.CoachRepository;
import com.iso.hypo.repositories.CourseRepository;
import com.iso.hypo.repositories.GymRepository;
import com.iso.hypo.repositories.MemberRepository;
import com.iso.hypo.repositories.MembershipPlanRepository;
import com.iso.hypo.tests.data.Populator;

import net.datafaker.Faker;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(Lifecycle.PER_CLASS)
@Tag("populator")
class PopulatorTests {

	@LocalServerPort
	private int port;

	@Autowired
	BrandRepository brandRepository;
	@Autowired
	GymRepository gymRepository;
	@Autowired
	CoachRepository coachRepository;
	@Autowired
	CourseRepository courseRepository;
	@Autowired
	MembershipPlanRepository membershipPlanRepository;
	@Autowired
	MemberRepository memberRepository;
	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	ModelMapper modelMapper;

	private Faker faker = new Faker();

	@Test
	void populator() {
		brandRepository.deleteAll();
		gymRepository.deleteAll();
		coachRepository.deleteAll();
		courseRepository.deleteAll();
		memberRepository.deleteAll();

		for (int i = 0; i < 10; i++) {
			Brand item = BrandBuilder.build(faker.code().isbn10(), faker.company().name());
			brandRepository.save(item);
		}

		for (int i = 0; i < 5; i++) {
			Brand item = BrandBuilder.build(faker.code().isbn10(), faker.company().name());
			item.setActive(false);
			brandRepository.save(item);
		}

		Populator populator = new Populator(brandRepository, gymRepository, coachRepository, courseRepository,
				membershipPlanRepository, memberRepository);
		populator.populateFullBrand("crossfitextreme", "Crossfit Extreme");
		populator.populateFullBrand("fitnessboxing", "Fitness Boxing");
	}

}

