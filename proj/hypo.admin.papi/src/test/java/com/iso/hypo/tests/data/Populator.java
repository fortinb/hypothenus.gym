package com.iso.hypo.tests.data;

import java.net.MalformedURLException;
import java.net.URI;
import java.sql.Date;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.iso.hypo.admin.papi.dto.model.BrandDto;
import com.iso.hypo.admin.papi.dto.model.UserDto;
import com.iso.hypo.admin.papi.dto.post.PostMemberDto;
import com.iso.hypo.domain.CoachBuilder;
import com.iso.hypo.domain.CourseBuilder;
import com.iso.hypo.domain.GymBuilder;
import com.iso.hypo.domain.LocalizedString;
import com.iso.hypo.domain.MemberBuilder;
import com.iso.hypo.domain.MembershipPlanBuilder;
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
import com.iso.hypo.domain.security.Roles;
import com.iso.hypo.repositories.CoachRepository;
import com.iso.hypo.repositories.CourseRepository;
import com.iso.hypo.repositories.GymRepository;
import com.iso.hypo.repositories.MemberRepository;
import com.iso.hypo.repositories.MembershipPlanRepository;
import com.iso.hypo.tests.http.HttpUtils;
import com.iso.hypo.tests.security.Users;

import net.datafaker.Faker;

public class Populator {

	// Use constructor injection instead of field injection
	private final GymRepository gymRepository;
	private final CoachRepository coachRepository;
	private final MemberRepository memberRepository;
	private final CourseRepository courseRepository;
	private final MembershipPlanRepository membershipPlanRepository;
	private final TestRestTemplate testRestTemplate;
	private final ModelMapper modelMapper;
	private final int port;
	
	@Autowired
	public Populator(GymRepository gymRepository, 
					 CoachRepository coachRepository, 
					 CourseRepository courseRepository,
					 MembershipPlanRepository membershipPlanRepository, 
					 MemberRepository memberRepository,
					 ModelMapper modelMapper,
					 TestRestTemplate testRestTemplate,
					 int port) {
		this.gymRepository = gymRepository;
		this.coachRepository = coachRepository;
		this.memberRepository = memberRepository;
		this.courseRepository = courseRepository;
		this.membershipPlanRepository = membershipPlanRepository;
		this.modelMapper = modelMapper;
		this.testRestTemplate = testRestTemplate;
		this.port = port;
	}

	private Faker faker = new Faker();

	public BrandDto populateFullBrand(BrandDto brand, UserDto user) throws JsonProcessingException, MalformedURLException {
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

		gym_boucherville = GymBuilder.build(brand.getUuid(), gymCode_boucherville, "Studio Boucherville", coachs.subList(0, 2));
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
		if (user != null) {
			createUserMember(brand, user, gym_boucherville.getUuid());
		}
		
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

		// Promotional Membership plans
		MembershipPlan membershipPlan = new MembershipPlan(
				brandUuid,
				buildName("25 cours au prix de 20", "25 classes for the price of 20"),
				buildTitle("PROMOTION HYROX 2026", "HYROX 2026 PROMO"),
				buildDescription("Prépare toi pour HYROX 2026 !",
								 "Get ready for HYROX 2026 !"),
				buildTermsOfUse("Réservation requise; Valide dans plusieurs de nos studios; Aucune date d'expiration; Non-transferable;",
							"Reservation required; Valid in most of our locations; No expiration date; Non-transferable;"),
				25,	MembershipPlanPeriodEnum.trial,	BillingFrequencyEnum.oneTime,
				BuildCost(34999),0,includedGyms,includedCourses,Date.from(Instant.now().minus(7, ChronoUnit.DAYS)),Date.from(Instant.now().plus(30, ChronoUnit.DAYS)),
				false,false,true,true,	Instant.now().minus(7, ChronoUnit.DAYS),null);
		membershipPlan.setUuid(java.util.UUID.randomUUID().toString());
		membershipPlanRepository.save(membershipPlan);
		membershipPlans.add(membershipPlan);

		// Trial Membership plans
		membershipPlan = new MembershipPlan(
				brandUuid,
				buildName("Premier cour", "Trial class"),
				buildTitle("VIENS ESSAYER", "COME AND TRY"),
				buildDescription("Ton premier cours pour 10$ seulement.",
								 "Your first class for only $10. "),
				buildTermsOfUse("Réservation requise; Valide dans plusieurs de nos studios; Aucune date d'expiration; Non-transferable; Nouveaux membres seulement;",
							"Reservation required; Valid in most of our locations; No expiration date; Non-transferable; New members only;"),
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
				buildTermsOfUse("Réservation requise; Valide dans plusieurs de nos studios; Aucune date d'expiration; Non transférable; Nouveaux membres seulement;",
							"Réservation requise; Valid in most of our locations; No expiration date; Non-transferable; New members only;"),
				10,	MembershipPlanPeriodEnum.trial,	BillingFrequencyEnum.oneTime,
				BuildCost(14900),0,includedGyms,includedCourses,Date.from(Instant.now().minus(7, ChronoUnit.DAYS)),null,
				false,false,false,true,	Instant.now().minus(7, ChronoUnit.DAYS),null);
		membershipPlan.setUuid(java.util.UUID.randomUUID().toString());
		membershipPlanRepository.save(membershipPlan);
		membershipPlans.add(membershipPlan);
		
		// Regular Membership plans
		membershipPlan = new MembershipPlan(
				brandUuid,
				buildName("1 cour", "1 class"),
				buildTitle("POUR TE DONNER LE GOÛT DE REVENIR", "TO GIVE YOU A REASON TO COME BACK"),
				buildDescription("Utilise ton cour dans tout nos studios.",
								 "Use your class in all our locations."),
				buildTermsOfUse("Réservation requise; Valide dans plusieurs de nos studios; Aucune date d'expiration; Non transférable;",
							"Réservation requise; Valid in most of our locations; No expiration date; Non-transferable; "),
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
				buildTermsOfUse("Partageable avec un ami; Réservation requise; Valide dans plusieurs de nos studios; Aucune date d'expiration; Non transférable;",
							"Shareable with a friend; Reservation required; Valid in most of our locations; No expiration date; Non-transferable;"),
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
				buildTermsOfUse("Partageable avec un ami; Réservation requise; Valide dans plusieurs de nos studios; Aucune date d'expiration; Non transférable;",
							"Shareable with a friend; Reservation required; Valid in most of our locations; No expiration date; Non-transferable;"),
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
				buildTermsOfUse("Partageable avec un ami; Réservation requise; Valide dans plusieurs de nos studios; Aucune date d'expiration; Non transférable;",
							"Shareable with a friend; Reservation required; Valid in most of our locations; No expiration date; Non-transferable;"),
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
				buildTermsOfUse("Abonnement récurrent; 4 cours tous les 28 jours; Réservation requise; Valide dans plusieurs de nos studios; Non transférable; Aucun frais d'annulation avec 30 jours de préavis; Possibilité de mettre ton abonnement sur pause 2 fois par année;",
							"Recurring subscription; 4 classes every 28 days; Reservation required; Valid in most of our locations; Non-transferable; No cancellation fee with 30 days notice; Possibility to put your subscription on hold 2 times a year;"),
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
				buildTermsOfUse("Abonnement récurrent; 8 cours tous les 28 jours; Réservation requise; Valide dans plusieurs de nos studios; Non transférable; Aucun frais d'annulation avec 30 jours de préavis; Possibilité de mettre ton abonnement sur pause 2 fois par année;",
							"Recurring subscription; 8 classes every 28 days; Reservation required; Valid in most of our locations; Non-transferable; No cancellation fee with 30 days notice; Possibility to put your subscription on hold 2 times a year;"),
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
				buildTermsOfUse("Abonnement récurrent; 12 cours tous les 28 jours; Réservation requise; Valide dans plusieurs de nos studios; Non transférable; Aucun frais d'annulation avec 30 jours de préavis; Possibilité de mettre ton abonnement sur pause 2 fois par année;",
							"Recurring subscription; 12 classes every 28 days; Reservation required; Valid in most of our locations; Non-transferable; No cancellation fee with 30 days notice; Possibility to put your subscription on hold 2 times a year;"),
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
				buildTermsOfUse("Abonnement récurrent; 24 cours tous les 28 jours; Réservation requise; Valide dans plusieurs de nos studios; Non transférable; Aucun frais d'annulation avec 30 jours de préavis; Possibilité de mettre ton abonnement sur pause 2 fois par année;",
							"Recurring subscription; 24 classes every 28 days; Reservation required; Valid in most of our locations; Non-transferable; No cancellation fee with 30 days notice; Possibility to put your subscription on hold 2 times a year;"),
				6,	MembershipPlanPeriodEnum.weekly, BillingFrequencyEnum.monthly,
				BuildCost(17499),12,includedGyms,includedCourses,Date.from(Instant.now().minus(7, ChronoUnit.DAYS)),null,
				false,false,false,true,	Instant.now().minus(7, ChronoUnit.DAYS),null);
		membershipPlan.setUuid(java.util.UUID.randomUUID().toString());
		membershipPlanRepository.save(membershipPlan);
		membershipPlans.add(membershipPlan);
		
		// Gift card Membership plans
		membershipPlan = new MembershipPlan(
				brandUuid,
				buildName("10 cours", "10 classes"),
				buildTitle("CARTE-CADEAU 10 COURS", "GIFT CARD 10 CLASSES"),
				buildDescription("Fait bouger un ami !",
								 "Get a friend moving !"),
				buildTermsOfUse("La carte-cadeau est envoyée par courriel dans les 24h; Réservation requise; Valide dans plusieurs de nos studios; Aucune date d'expiration; Non transférable;",
							"The gift card is sent by email within 24h; Reservation required; Valid in most of our locations; No expiration date; Non-transferable;"),
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
				buildDescription("Le meilleur cadeau pour un ami qui veut se remettre en forme !",
						 		 "The best gift for a friend who wants to get back in shape !"),
				buildTermsOfUse("La carte-cadeau est envoyée par courriel dans les 24h; Valide dans plusieurs de nos studios; Aucune date d'expiration;",
							"The gift card is sent by email within 24h; Shareable with a friend; Reservation required; Valid in most of our locations; No expiration date; Non-transferable;"),
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
				buildTermsOfUse("La carte-cadeau est envoyée par courriel dans les 24h; Utilisable pour tout achat en studio;",
							"The gift card is sent by email within 24h; Redeemable for any in-studio purchase;"),
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

	public static List<LocalizedString> buildTermsOfUse(String fr, String en) {
		ArrayList<LocalizedString> items = new ArrayList<LocalizedString>();
		items.add(new LocalizedString(fr, LanguageEnum.fr));
		items.add(new LocalizedString(en, LanguageEnum.en));

		return items;
	}
	
	public static Cost BuildCost(int amount) {
		return new Cost(amount, new Currency("Canadian dollar","CAD","$"));
	}
	
	private void createUserMember(BrandDto brand, UserDto user, String preferredGymUuid) throws JsonProcessingException, MalformedURLException {
	    final String memberPostURI = "/v1/brands/%s/members/register";
	    
		PostMemberDto postDto = modelMapper.map(MemberBuilder.build(brand.getUuid(), MemberTypeEnum.regular), PostMemberDto.class);
		postDto.getPerson().setEmail(user.getEmail());
		postDto.getPerson().setFirstname(user.getFirstname());
		postDto.getPerson().setLastname(user.getLastname());
		postDto.setPreferredGymUuid(preferredGymUuid);
		
		HttpEntity<PostMemberDto> httpEntity = HttpUtils.createHttpEntity(Roles.Admin, Users.Admin, postDto);

		// Act
		ResponseEntity<JsonNode> response = testRestTemplate.exchange(HttpUtils.createURL(URI.create(String.format(memberPostURI, brand.getUuid())), port, null),
				HttpMethod.POST, httpEntity, JsonNode.class);

		Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode(),
				String.format("Post error: %s", response.getStatusCode()));
	}
}
