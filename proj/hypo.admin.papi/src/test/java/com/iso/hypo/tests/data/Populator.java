package com.iso.hypo.tests.data;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.iso.hypo.domain.BrandBuilder;
import com.iso.hypo.domain.CoachBuilder;
import com.iso.hypo.domain.MemberBuilder;
import com.iso.hypo.domain.CourseBuilder;
import com.iso.hypo.domain.GymBuilder;
import com.iso.hypo.domain.MembershipPlanBuilder;
import com.iso.hypo.domain.aggregate.Brand;
import com.iso.hypo.domain.aggregate.Coach;
import com.iso.hypo.domain.aggregate.Member;
import com.iso.hypo.domain.aggregate.Course;
import com.iso.hypo.domain.aggregate.Gym;
import com.iso.hypo.domain.aggregate.MembershipPlan;
import com.iso.hypo.domain.enumeration.MemberTypeEnum;
import com.iso.hypo.repositories.BrandRepository;
import com.iso.hypo.repositories.CoachRepository;
import com.iso.hypo.repositories.MemberRepository;
import com.iso.hypo.repositories.CourseRepository;
import com.iso.hypo.repositories.GymRepository;
import com.iso.hypo.repositories.MembershipPlanRepository;

import net.datafaker.Faker;

public class Populator {

	// Use constructor injection instead of field injection
	private final BrandRepository brandRepository;
	private final GymRepository gymRepository;
	private final CoachRepository coachRepository;
	private final MemberRepository memberRepository;
	private final CourseRepository courseRepository;
	private final MembershipPlanRepository membershipPlanRepository;

	@Autowired
	public Populator(BrandRepository brandRepository, GymRepository gymRepository, CoachRepository coachRepository,
			CourseRepository courseRepository,MembershipPlanRepository membershipPlanRepository, MemberRepository memberRepository) {
		this.brandRepository = brandRepository;
		this.gymRepository = gymRepository;
		this.coachRepository = coachRepository;
		this.memberRepository = memberRepository;
		this.courseRepository = courseRepository;
		this.membershipPlanRepository = membershipPlanRepository;
	}

	private Faker faker = new Faker();

	public Brand populateFullBrand(String brandCode, String brandName) {
		Brand brand;

		brand = BrandBuilder.build(brandCode, brandName);
		brand = brandRepository.save(brand);

		// Gyms
		final String gymCode_boucherville = "boucherville";
		final String gymCode_longueuil = "longueuil";

		Gym gym_boucherville;
		Gym gym_longueuil;

		gym_boucherville = GymBuilder.build(brand.getUuid(), gymCode_boucherville, "Studio Boucherville");
		gym_boucherville = gymRepository.save(gym_boucherville);

		gym_longueuil = GymBuilder.build(brand.getUuid(), gymCode_longueuil, "Studio Longueuil");
		gym_longueuil = gymRepository.save(gym_longueuil);

		for (int i = 0; i < 10; i++) {
			Gym item = GymBuilder.build(brand.getUuid(), faker.code().isbn10(), faker.company().name());
			gymRepository.save(item);
		}

		for (int i = 0; i < 5; i++) {
			Gym item = GymBuilder.build(brand.getUuid(), faker.code().isbn10(), faker.company().name());
			item.setActive(false);
			gymRepository.save(item);
		}

		// Coaches
		List<Coach> coachs_boucherville = new ArrayList<Coach>();
		List<Coach> coachs_longueuil = new ArrayList<Coach>();

		for (int i = 0; i < 10; i++) {
			Coach item = CoachBuilder.build(brand.getUuid(), gym_boucherville.getUuid());
			item.setActive(true);
			item = coachRepository.save(item);
			coachs_boucherville.add(item);

			item = CoachBuilder.build(brand.getUuid(), gym_longueuil.getUuid());
			item.setActive(true);
			item = coachRepository.save(item);
			coachs_longueuil.add(item);
		}

		for (int i = 0; i < 5; i++) {
			Coach item = CoachBuilder.build(brand.getUuid(), gym_boucherville.getUuid());
			item.setActive(false);
			coachRepository.save(item);

			item = CoachBuilder.build(brand.getUuid(), gym_longueuil.getUuid());
			item.setActive(false);
			coachRepository.save(item);
		}

		// Courses
		for (int i = 0; i < 10; i++) {
			Course item = CourseBuilder.build(brand.getUuid(), gym_boucherville.getUuid(), coachs_boucherville);
			item = courseRepository.save(item);
		}

		for (int i = 0; i < 4; i++) {
			Course item = CourseBuilder.build(brand.getUuid(), gym_longueuil.getUuid(), coachs_longueuil);
			item = courseRepository.save(item);
		}

		// Membership plans
		for (int i = 0; i < 4; i++) {
			MembershipPlan item = MembershipPlanBuilder.build(brand.getUuid());
			membershipPlanRepository.save(item);
		}

		for (int i = 0; i < 2; i++) {
			MembershipPlan item = MembershipPlanBuilder.build(brand.getUuid());
			item.setActive(false);
			membershipPlanRepository.save(item);
		}

		// Members
		List<Member> members_boucherville = new ArrayList<Member>();
		List<Member> members_longueuil = new ArrayList<Member>();

		for (int i = 0; i < 10; i++) {
			Member item = MemberBuilder.build(brand.getUuid(), MemberTypeEnum.regular);
			item.setPreferredGymUuid(gym_boucherville.getUuid());
			item.setActive(true);
			item = memberRepository.save(item);
			members_boucherville.add(item);

			item = MemberBuilder.build(brand.getUuid(), MemberTypeEnum.regular);
			item.setPreferredGymUuid(gym_longueuil.getUuid());
			item.setActive(true);
			item = memberRepository.save(item);
			members_longueuil.add(item);
		}

		for (int i = 0; i < 5; i++) {
			Member item = MemberBuilder.build(brand.getUuid(), MemberTypeEnum.regular);
			item.setPreferredGymUuid(gym_boucherville.getUuid());
			item.setActive(false);
			memberRepository.save(item);

			item = MemberBuilder.build(brand.getUuid(), MemberTypeEnum.regular);
			item.setPreferredGymUuid(gym_longueuil.getUuid());
			item.setActive(false);
			memberRepository.save(item);
		}

		return brand;
	}
}