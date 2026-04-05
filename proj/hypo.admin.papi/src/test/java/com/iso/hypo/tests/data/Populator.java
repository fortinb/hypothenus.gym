package com.iso.hypo.tests.data;

import java.sql.Date;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.iso.hypo.admin.papi.dto.model.BrandDto;
import com.iso.hypo.domain.CoachBuilder;
import com.iso.hypo.domain.CourseBuilder;
import com.iso.hypo.domain.GymBuilder;
import com.iso.hypo.domain.MemberBuilder;
import com.iso.hypo.domain.MembershipPlanBuilder;
import com.iso.hypo.domain.LocalizedString;
import com.iso.hypo.domain.aggregate.Coach;
import com.iso.hypo.domain.aggregate.Course;
import com.iso.hypo.domain.aggregate.Gym;
import com.iso.hypo.domain.aggregate.Member;
import com.iso.hypo.domain.aggregate.MembershipPlan;
import com.iso.hypo.domain.enumeration.BillingFrequencyEnum;
import com.iso.hypo.domain.enumeration.LanguageEnum;
import com.iso.hypo.domain.enumeration.MemberTypeEnum;
import com.iso.hypo.domain.enumeration.MembershipPlanPeriodEnum;
import com.iso.hypo.domain.pricing.Cost;
import com.iso.hypo.domain.pricing.Currency;
import com.iso.hypo.repositories.CoachRepository;
import com.iso.hypo.repositories.CourseRepository;
import com.iso.hypo.repositories.GymRepository;
import com.iso.hypo.repositories.MemberRepository;
import com.iso.hypo.repositories.MembershipPlanRepository;

import net.datafaker.Faker;

public class Populator {

	// Use constructor injection instead of field injection
	private final GymRepository gymRepository;
	private final CoachRepository coachRepository;
	private final MemberRepository memberRepository;
	private final CourseRepository courseRepository;
	private final MembershipPlanRepository membershipPlanRepository;

	@Autowired
	public Populator(GymRepository gymRepository, CoachRepository coachRepository, CourseRepository courseRepository,
			MembershipPlanRepository membershipPlanRepository, MemberRepository memberRepository) {
		this.gymRepository = gymRepository;
		this.coachRepository = coachRepository;
		this.memberRepository = memberRepository;
		this.courseRepository = courseRepository;
		this.membershipPlanRepository = membershipPlanRepository;
	}

	private Faker faker = new Faker();

	public BrandDto populateFullBrand(BrandDto brand) {
		// Gyms
		final String gymCode_boucherville = "boucherville";
		final String gymCode_longueuil = "longueuil";

		// Coaches
		List<Coach> coachs = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			Coach item = CoachBuilder.build(brand.getUuid());
			item.setActive(true);
			coachRepository.save(item);
			coachs.add(item);
		}

		for (int i = 0; i < 5; i++) {
			Coach item = CoachBuilder.build(brand.getUuid());
			item.setActive(false);
			coachRepository.save(item);
		}

		Gym gym_boucherville;
		Gym gym_longueuil;

		gym_boucherville = GymBuilder.build(brand.getUuid(), gymCode_boucherville, "Studio Boucherville",
				coachs.subList(0, 2));
		gym_boucherville = gymRepository.save(gym_boucherville);

		gym_longueuil = GymBuilder.build(brand.getUuid(), gymCode_longueuil, "Studio Longueuil", coachs.subList(0, 4));
		gym_longueuil = gymRepository.save(gym_longueuil);

		List<Gym> gyms = new ArrayList<Gym>();
		gyms.add(gym_boucherville);
		gyms.add(gym_longueuil);
		for (int i = 0; i < 10; i++) {
			Gym item = GymBuilder.build(brand.getUuid(), faker.code().isbn10(), faker.company().name(),
					coachs.subList(0, 2));
			gymRepository.save(item);
			gyms.add(item);
		}

		for (int i = 0; i < 5; i++) {
			Gym item = GymBuilder.build(brand.getUuid(), faker.code().isbn10(), faker.company().name(), null);
			item.setActive(false);
			gymRepository.save(item);
		}

		// Courses
		List<Course> courses = new ArrayList<Course>();
		for (int i = 0; i < 10; i++) {
			Course item = CourseBuilder.build(brand.getUuid());
			item = courseRepository.save(item);
			courses.add(item);
		}

		for (int i = 0; i < 4; i++) {
			Course item = CourseBuilder.build(brand.getUuid());
			item = courseRepository.save(item);
		}

		buildMembershipPlan(brand.getUuid(), gyms, courses);

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

	public List<MembershipPlan> buildMembershipPlan(String brandUuid, List<Gym> includedGyms, List<Course> includedCourses) {
		List<MembershipPlan> membershipPlans = new ArrayList<>();

		MembershipPlan membershipPlan = new MembershipPlan(
				brandUuid,
				buildName("Premier cour", "Trial class"),
				buildTitle("VIENS ESSAYER", "COME AND TRY"),
				buildDescription("Ton premier cours pour 10$ seulement.",
								 "Your first class for only $10. "),
				buildDetail("Réservation requise. Valide dans tous nos studios. Aucune date d'expiration. Non-transferable. Nouveaux membres seulement.",
							"Reservation required. Valid in all our locations. No expiration date. Non-transferable. New members only."),
				1,	MembershipPlanPeriodEnum.trial,	BillingFrequencyEnum.oneTime,
				BuildCost(1000),0,includedGyms,includedCourses,Date.from(Instant.now().minus(7, ChronoUnit.DAYS)),null,
				false,false,false,true,	Instant.now().minus(7, ChronoUnit.DAYS),null);
		membershipPlan.setUuid(java.util.UUID.randomUUID().toString());
		membershipPlanRepository.save(membershipPlan);
		membershipPlans.add(membershipPlan);

		 membershipPlan = new MembershipPlan(
				brandUuid,
				buildName("Promo nouveaux membres - 10 cours", "New Members Promo - 10 classes"),
				buildTitle("DÉCOUVRE NOS COURS", "DISCOVER OUR CLASSES"),
				buildDescription("Offre exclusive pour les nouveaux membres.",
								 "Exclusive offer for new members."),
				buildDetail("Réservation requise. Valide dans tous nos studios. Aucune date d'expiration. Non transférable. Nouveaux membres seulement.",
							"Réservation requise. Valid in all our locations. No expiration date. Non-transferable. New members only."),
				10,	MembershipPlanPeriodEnum.trial,	BillingFrequencyEnum.oneTime,
				BuildCost(14900),0,includedGyms,includedCourses,Date.from(Instant.now().minus(7, ChronoUnit.DAYS)),null,
				false,false,true,true,	Instant.now().minus(7, ChronoUnit.DAYS),null);
		membershipPlan.setUuid(java.util.UUID.randomUUID().toString());
		membershipPlanRepository.save(membershipPlan);
		membershipPlans.add(membershipPlan);
		
		membershipPlan = new MembershipPlan(
				brandUuid,
				buildName("1 cour", "1 class"),
				buildTitle("POUR TE DONNER LE GOÛT DE REVENIR", "TO GIVE YOU A REASON TO COME BACK"),
				buildDescription("Utilise ton cour dans tout nos studios.",
								 "Use your class in all our locations."),
				buildDetail("Réservation requise. Valide dans tous nos studios. Aucune date d'expiration. Non transférable.",
							"Réservation requise. Valid in all our locations. No expiration date. Non-transferable. "),
				1,	MembershipPlanPeriodEnum.classes,	BillingFrequencyEnum.oneTime,
				BuildCost(2400),0,includedGyms,includedCourses,Date.from(Instant.now().minus(7, ChronoUnit.DAYS)),null,
				false,false,false,true,	Instant.now().minus(7, ChronoUnit.DAYS),null);
		membershipPlan.setUuid(java.util.UUID.randomUUID().toString());
		membershipPlanRepository.save(membershipPlan);
		membershipPlans.add(membershipPlan);
		
		membershipPlan = new MembershipPlan(
				brandUuid,
				buildName("10 cours", "10 classes"),
				buildTitle("ASSEZ POUR VOIR DES RÉSULTATS", "ENOUGH TO SEE RESULTS"),
				buildDescription("Utilise tes cours dans tout nos studios.",
						 		 "Use your classes in all our locations."),
				buildDetail("Partageable avec un ami. Réservation requise. Valide dans tous nos studios. Aucune date d'expiration. Non transférable.",
							"Shareable with a friend. Reservation required. Valid in all our locations. No expiration date. Non-transferable."),
				10,	MembershipPlanPeriodEnum.classes,	BillingFrequencyEnum.oneTime,
				BuildCost(19999),0,includedGyms,includedCourses,Date.from(Instant.now().minus(7, ChronoUnit.DAYS)),null,
				true,false,false,true,	Instant.now().minus(7, ChronoUnit.DAYS),null);
		membershipPlan.setUuid(java.util.UUID.randomUUID().toString());
		membershipPlanRepository.save(membershipPlan);
		membershipPlans.add(membershipPlan);
		
		membershipPlan = new MembershipPlan(
				brandUuid,
				buildName("20 cours", "20 classes"),
				buildTitle("TON NOUVEAU RYTHME DE CROISIÈRE", "YOUR NEW CRUISING PACE"),
				buildDescription("Utilise tes cours dans tout nos studios.",
				 		 		 "Use your classes in all our locations."),
				buildDetail("Partageable avec un ami. Réservation requise. Valide dans tous nos studios. Aucune date d'expiration. Non transférable.",
							"Shareable with a friend. Reservation required. Valid in all our locations. No expiration date. Non-transferable."),
				20,	MembershipPlanPeriodEnum.classes,	BillingFrequencyEnum.oneTime,
				BuildCost(34999),0,includedGyms,includedCourses,Date.from(Instant.now().minus(7, ChronoUnit.DAYS)),null,
				true,false,false,true,	Instant.now().minus(7, ChronoUnit.DAYS),null);
		membershipPlan.setUuid(java.util.UUID.randomUUID().toString());
		membershipPlanRepository.save(membershipPlan);
		membershipPlans.add(membershipPlan);
		
		membershipPlan = new MembershipPlan(
				brandUuid,
				buildName("50 cours", "50 classes"),
				buildTitle("L'INVESTISSEMENT LE PLUS PAYANT", "THE BEST DEAL"),
				buildDescription("Utilise tes cours dans tout nos studios.",
				 		 		 "Use your classes in all our locations."),
				buildDetail("Partageable avec un ami. Réservation requise. Valide dans tous nos studios. Aucune date d'expiration. Non transférable.",
							"Shareable with a friend. Reservation required. Valid in all our locations. No expiration date. Non-transferable."),
				50,	MembershipPlanPeriodEnum.classes,	BillingFrequencyEnum.oneTime,
				BuildCost(74999),0,includedGyms,includedCourses,Date.from(Instant.now().minus(7, ChronoUnit.DAYS)),null,
				true,false,false,true,	Instant.now().minus(7, ChronoUnit.DAYS),null);
		membershipPlan.setUuid(java.util.UUID.randomUUID().toString());
		membershipPlanRepository.save(membershipPlan);
		membershipPlans.add(membershipPlan);
		
		membershipPlan = new MembershipPlan(
				brandUuid,
				buildName("1x /semaine", "1x /week"),
				buildTitle("POUR GARDER LE RYTHME", "TO KEEP THE PACE"),
				buildDescription("Ton prix reste fixe à vie! ",
								 "Your price is fixed for life!"),
				buildDetail("Abonnement récurrent. 4 cours tous les 28 jours. Réservation requise. Valide dans tous nos studios. Non transférable. Aucun frais d'annulation avec 30 jours de préavis. Possibilité de mettre ton abonnement sur pause 2 fois par année.",
							"Recurring subscription. 4 classes every 28 days. Reservation required. Valid in all our locations. Non-transferable. No cancellation fee with 30 days notice. Possibility to put your subscription on hold 2 times a year."),
				1,	MembershipPlanPeriodEnum.weekly, BillingFrequencyEnum.monthly,
				BuildCost(7499),12,includedGyms,includedCourses,Date.from(Instant.now().minus(7, ChronoUnit.DAYS)),null,
				false,false,false,true,	Instant.now().minus(7, ChronoUnit.DAYS),null);
		membershipPlan.setUuid(java.util.UUID.randomUUID().toString());
		membershipPlanRepository.save(membershipPlan);
		membershipPlans.add(membershipPlan);
		
		membershipPlan = new MembershipPlan(
				brandUuid,
				buildName("2x /semaine", "2x /week"),
				buildTitle("POUR VOIR DU PROGRÈS", "TO SEE PROGRESS"),
				buildDescription("Ton prix reste fixe à vie ! ",
						 		 "Your price is fixed for life !"),
				buildDetail("Abonnement récurrent. 8 cours tous les 28 jours. Réservation requise. Valide dans tous nos studios. Non transférable. Aucun frais d'annulation avec 30 jours de préavis. Possibilité de mettre ton abonnement sur pause 2 fois par année.",
							"Recurring subscription. 8 classes every 28 days. Reservation required. Valid in all our locations. Non-transferable. No cancellation fee with 30 days notice. Possibility to put your subscription on hold 2 times a year."),
				2,	MembershipPlanPeriodEnum.weekly, BillingFrequencyEnum.monthly,
				BuildCost(12499),12,includedGyms,includedCourses,Date.from(Instant.now().minus(7, ChronoUnit.DAYS)),null,
				false,false,false,true,	Instant.now().minus(7, ChronoUnit.DAYS),null);
		membershipPlan.setUuid(java.util.UUID.randomUUID().toString());
		membershipPlanRepository.save(membershipPlan);
		membershipPlans.add(membershipPlan);
		
		membershipPlan = new MembershipPlan(
				brandUuid,
				buildName("3x /semaine", "3x /week"),
				buildTitle("POUR TE DÉPASSER", "TO PUSH YOUR LIMITS"),
				buildDescription("Ton prix reste fixe à vie ! ",
				 		 		 "Your price is fixed for life !"),
				buildDetail("Abonnement récurrent. 12 cours tous les 28 jours. Réservation requise. Valide dans tous nos studios. Non transférable. Aucun frais d'annulation avec 30 jours de préavis. Possibilité de mettre ton abonnement sur pause 2 fois par année.",
							"Recurring subscription. 12 classes every 28 days. Reservation required. Valid in all our locations. Non-transferable. No cancellation fee with 30 days notice. Possibility to put your subscription on hold 2 times a year."),
				3,	MembershipPlanPeriodEnum.weekly, BillingFrequencyEnum.monthly,
				BuildCost(16499),12,includedGyms,includedCourses,Date.from(Instant.now().minus(7, ChronoUnit.DAYS)),null,
				false,false,false,true,	Instant.now().minus(7, ChronoUnit.DAYS),null);
		membershipPlan.setUuid(java.util.UUID.randomUUID().toString());
		membershipPlanRepository.save(membershipPlan);
		membershipPlans.add(membershipPlan);
		
		membershipPlan = new MembershipPlan(
				brandUuid,
				buildName("6x / semaine", "6x / week"),
				buildTitle("POUR TOUT CASSER", "TO BREAK ALL THE LIMITS"),
				buildDescription("Ton prix reste fixe à vie ! ",
		 		 		 		 "Your price is fixed for life !"),
				buildDetail("Abonnement récurrent. 24 cours tous les 28 jours. Réservation requise. Valide dans tous nos studios. Non transférable. Aucun frais d'annulation avec 30 jours de préavis. Possibilité de mettre ton abonnement sur pause 2 fois par année.",
							"Recurring subscription. 24 classes every 28 days. Reservation required. Valid in all our locations. Non-transferable. No cancellation fee with 30 days notice. Possibility to put your subscription on hold 2 times a year."),
				6,	MembershipPlanPeriodEnum.weekly, BillingFrequencyEnum.monthly,
				BuildCost(17499),12,includedGyms,includedCourses,Date.from(Instant.now().minus(7, ChronoUnit.DAYS)),null,
				false,false,false,true,	Instant.now().minus(7, ChronoUnit.DAYS),null);
		membershipPlan.setUuid(java.util.UUID.randomUUID().toString());
		membershipPlanRepository.save(membershipPlan);
		membershipPlans.add(membershipPlan);
		
		membershipPlan = new MembershipPlan(
				brandUuid,
				buildName("10 cours", "10 classes"),
				buildTitle("CARTE-CADEAU 10 COURS", "GIFT CARD 10 CLASSES"),
				buildDescription("Fait bouger un ami !",
								 "Get a friend moving !"),
				buildDetail("La carte-cadeau est envoyée par courriel dans les 24h. Réservation requise. Valide dans tous nos studios. Aucune date d'expiration. Non transférable.",
							"The gift card is sent by email within 24h. Reservation required. Valid in all our locations. No expiration date. Non-transferable."),
				10,	MembershipPlanPeriodEnum.classes, BillingFrequencyEnum.oneTime,
				BuildCost(19999),0,includedGyms,includedCourses,Date.from(Instant.now().minus(7, ChronoUnit.DAYS)),null,
				false,true,false,true,	Instant.now().minus(7, ChronoUnit.DAYS),null);
		membershipPlan.setUuid(java.util.UUID.randomUUID().toString());
		membershipPlanRepository.save(membershipPlan);
		membershipPlans.add(membershipPlan);
		
		membershipPlan = new MembershipPlan(
				brandUuid,
				buildName("20 cours", "20 classes"),
				buildTitle("CARTE-CADEAU 20 COURS", "GIFT CARD 20 CLASSES"),
				buildDescription("Le meilleur cadeaux pour un ami qui veut se remettre en forme !",
						 		 "The best gift for a friend who wants to get back in shape !"),
				buildDetail("La carte-cadeau est envoyée par courriel dans les 24h. Valide dans tous nos studios. Aucune date d'expiration.",
							"The gift card is sent by email within 24h. Shareable with a friend. Reservation required. Valid in all our locations. No expiration date. Non-transferable."),
				10,	MembershipPlanPeriodEnum.classes, BillingFrequencyEnum.oneTime,
				BuildCost(34999),0,includedGyms,includedCourses,Date.from(Instant.now().minus(7, ChronoUnit.DAYS)),null,
				false,true,false,true,	Instant.now().minus(7, ChronoUnit.DAYS),null);
		membershipPlan.setUuid(java.util.UUID.randomUUID().toString());
		membershipPlanRepository.save(membershipPlan);
		membershipPlans.add(membershipPlan);
		
		membershipPlan = new MembershipPlan(
				brandUuid,
				buildName("Carte cadeau 50$", "Gift card 50$"),
				buildTitle("OFFRE EN CADEAU", "GIFT OFFER"),
				buildDescription("Carte-cadeau d'une valeur de 50$",
								 "Gift card with a value of 50$"),
				buildDetail("La carte-cadeau est envoyée par courriel dans les 24h. Utilisable pour tout achat en studio.",
							"The gift card is sent by email within 24h. Redeemable for any in-studio purchase."),
				0,	MembershipPlanPeriodEnum.amount, BillingFrequencyEnum.oneTime,
				BuildCost(5000),0,includedGyms,includedCourses,Date.from(Instant.now().minus(7, ChronoUnit.DAYS)),null,
				false,true,false,true,	Instant.now().minus(7, ChronoUnit.DAYS),null);
		membershipPlan.setUuid(java.util.UUID.randomUUID().toString());
		membershipPlanRepository.save(membershipPlan);
		membershipPlans.add(membershipPlan);
		
		// Inactive Membership plans
		for (int i = 0; i < 2; i++) {
			MembershipPlan item = MembershipPlanBuilder.build(brandUuid, null, null);
			item.setActive(false);
			membershipPlanRepository.save(item);
		}

		return membershipPlans;
	}
	
	public static List<LocalizedString> buildName(String fr, String en) {
		ArrayList<LocalizedString> items = new ArrayList<LocalizedString>();
		items.add(new LocalizedString(fr, LanguageEnum.fr));
		items.add(new LocalizedString(en, LanguageEnum.en));
		return items;
	}

	public static List<LocalizedString> buildTitle(String fr, String en) {
		ArrayList<LocalizedString> items = new ArrayList<LocalizedString>();
		items.add(new LocalizedString(fr, LanguageEnum.fr));
		items.add(new LocalizedString(en, LanguageEnum.en));
		return items;
	}

	public static List<LocalizedString> buildDescription(String fr, String en) {
		ArrayList<LocalizedString> items = new ArrayList<LocalizedString>();
		items.add(new LocalizedString(fr, LanguageEnum.fr));
		items.add(new LocalizedString(en, LanguageEnum.en));

		return items;
	}

	public static List<LocalizedString> buildDetail(String fr, String en) {
		ArrayList<LocalizedString> items = new ArrayList<LocalizedString>();
		items.add(new LocalizedString(fr, LanguageEnum.fr));
		items.add(new LocalizedString(en, LanguageEnum.en));

		return items;
	}
	
	public static Cost BuildCost(int amount) {
		return new Cost(amount, new Currency("Canadian dollar","CAD","$"));
	}
}