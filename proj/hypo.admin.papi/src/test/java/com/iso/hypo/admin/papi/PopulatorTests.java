package com.iso.hypo.admin.papi;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iso.hypo.domain.aggregate.Brand;
import com.iso.hypo.domain.aggregate.MembershipPlan;
import com.iso.hypo.repositories.BrandRepository;
import com.iso.hypo.repositories.MembershipPlanRepository;
import com.iso.hypo.domain.aggregate.Coach;
import com.iso.hypo.domain.aggregate.Course;
import com.iso.hypo.domain.aggregate.Gym;
import com.iso.hypo.repositories.CoachRepository;
import com.iso.hypo.repositories.CourseRepository;
import com.iso.hypo.repositories.GymRepository;
import com.iso.hypo.domain.BrandBuilder;
import com.iso.hypo.domain.CoachBuilder;
import com.iso.hypo.domain.CourseBuilder;
import com.iso.hypo.domain.GymBuilder;
import com.iso.hypo.domain.MembershipPlanBuilder;

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

		for (int i = 0; i < 10; i++) {
			Brand item = BrandBuilder.build(faker.code().isbn10(), faker.company().name());
			brandRepository.save(item);
		}

		for (int i = 0; i < 5; i++) {
			Brand item = BrandBuilder.build(faker.code().isbn10(), faker.company().name());
			item.setActive(false);
			brandRepository.save(item);
		}

		populateBrandCrossfit();
		populateBrandBoxing();
	}

	private void populateBrandCrossfit() {
		final String codeBrand_crossfit = "crossfitextreme";

		// brand
		Brand brand_crossfit;

		brand_crossfit = BrandBuilder.build(codeBrand_crossfit, "Crossfit Extreme");
		brand_crossfit = brandRepository.save(brand_crossfit);

		// Gyms
		final String gymCode_boucherville = "boucherville";
		final String gymCode_longueuil = "longueuil";

		Gym gym_boucherville;
		Gym gym_longueuil;

		gym_boucherville = GymBuilder.build(brand_crossfit.getUuid(), gymCode_boucherville, "Studio Boucherville");
		gym_boucherville = gymRepository.save(gym_boucherville);

		gym_longueuil = GymBuilder.build(brand_crossfit.getUuid(), gymCode_longueuil, "Studio Longueuil");
		gym_longueuil = gymRepository.save(gym_longueuil);

		for (int i = 0; i < 10; i++) {
			Gym item = GymBuilder.build(brand_crossfit.getUuid(), faker.code().isbn10(), faker.company().name());
			gymRepository.save(item);
		}

		for (int i = 0; i < 5; i++) {
			Gym item = GymBuilder.build(brand_crossfit.getUuid(), faker.code().isbn10(), faker.company().name());
			item.setActive(false);
			gymRepository.save(item);
		}

		// Coaches
		List<Coach> coachs_boucherville = new ArrayList<Coach>();
		List<Coach> coachs_longueuil = new ArrayList<Coach>();

		for (int i = 0; i < 10; i++) {
			Coach item = CoachBuilder.build(brand_crossfit.getUuid(), gym_boucherville.getUuid());
			item.setActive(true);
			item = coachRepository.save(item);
			coachs_boucherville.add(item);

			item = CoachBuilder.build(brand_crossfit.getUuid(), gym_longueuil.getUuid());
			item.setActive(true);
			item = coachRepository.save(item);
			coachs_longueuil.add(item);
		}

		for (int i = 0; i < 5; i++) {
			Coach item = CoachBuilder.build(brand_crossfit.getUuid(), gym_boucherville.getUuid());
			item.setActive(false);
			coachRepository.save(item);

			item = CoachBuilder.build(brand_crossfit.getUuid(), gym_longueuil.getUuid());
			item.setActive(false);
			coachRepository.save(item);
		}

		// Courses
		for (int i = 0; i < 10; i++) {
			Course item = CourseBuilder.build(brand_crossfit.getUuid(), gym_boucherville.getUuid(),
					coachs_boucherville);
			item = courseRepository.save(item);
		}

		for (int i = 0; i < 4; i++) {
			Course item = CourseBuilder.build(brand_crossfit.getUuid(), gym_longueuil.getUuid(), coachs_longueuil);
			item = courseRepository.save(item);
		}

		// Membership plans
		MembershipPlan item = MembershipPlanBuilder.build(brand_crossfit.getUuid());
		membershipPlanRepository.save(item);

		item = MembershipPlanBuilder.build(brand_crossfit.getUuid());
		item.setActive(false);
		membershipPlanRepository.save(item);
	}

	private void populateBrandBoxing() {
		final String codeBrand_boxing = "fitnessboxing";

		// brand
		Brand brand_boxing;

		brand_boxing = BrandBuilder.build(codeBrand_boxing, "Fitness Boxing");
		brand_boxing = brandRepository.save(brand_boxing);

		// Gyms
		final String gymCode_levis = "levis";
		final String gymCode_beloeil = "beloeil";

		Gym gym_levis;
		Gym gym_beloeil;

		gym_levis = GymBuilder.build(brand_boxing.getUuid(), gymCode_levis, "Studio Boucherville");
		gym_levis = gymRepository.save(gym_levis);

		gym_beloeil = GymBuilder.build(brand_boxing.getUuid(), gymCode_beloeil, "Studio Longueuil");
		gym_beloeil = gymRepository.save(gym_beloeil);

		for (int i = 0; i < 5; i++) {
			Gym item = GymBuilder.build(brand_boxing.getUuid(), faker.code().isbn10(), faker.company().name());
			gymRepository.save(item);
		}

		for (int i = 0; i < 2; i++) {
			Gym item = GymBuilder.build(brand_boxing.getUuid(), faker.code().isbn10(), faker.company().name());
			item.setActive(false);
			gymRepository.save(item);
		}

		// Coaches
		List<Coach> coachs_levis = new ArrayList<Coach>();
		List<Coach> coachs_beloeil = new ArrayList<Coach>();

		for (int i = 0; i < 5; i++) {
			Coach item = CoachBuilder.build(brand_boxing.getUuid(), gym_levis.getUuid());
			item.setActive(true);
			item = coachRepository.save(item);
			coachs_levis.add(item);

			item = CoachBuilder.build(brand_boxing.getUuid(), gym_beloeil.getUuid());
			item.setActive(true);
			item = coachRepository.save(item);
			coachs_beloeil.add(item);
		}

		for (int i = 0; i < 2; i++) {
			Coach item = CoachBuilder.build(brand_boxing.getUuid(), gym_levis.getUuid());
			item.setActive(false);
			coachRepository.save(item);

			item = CoachBuilder.build(brand_boxing.getUuid(), gym_beloeil.getUuid());
			item.setActive(false);
			coachRepository.save(item);
		}

		// Courses
		for (int i = 0; i < 5; i++) {
			Course item = CourseBuilder.build(brand_boxing.getUuid(), gym_levis.getUuid(),
					coachs_levis);
			item = courseRepository.save(item);
		}

		for (int i = 0; i < 2; i++) {
			Course item = CourseBuilder.build(brand_boxing.getUuid(), gym_beloeil.getUuid(), coachs_beloeil);
			item = courseRepository.save(item);
		}

		// Membership plans
		MembershipPlan item = MembershipPlanBuilder.build(brand_boxing.getUuid());
		membershipPlanRepository.save(item);

		item = MembershipPlanBuilder.build(brand_boxing.getUuid());
		item.setActive(false);
		membershipPlanRepository.save(item);

	}
}

