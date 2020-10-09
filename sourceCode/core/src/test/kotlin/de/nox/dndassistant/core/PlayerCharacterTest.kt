package de.nox.dndassistant.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.math.abs

class PlayerCharacterTest {

	private val log = LoggerFactory.getLogger("D&D PC-Test")

	@Test
	fun testBuilder() {
		log.info("PC: '${pc.name}' from '${pc.playername}'")

		assertEquals("Nox", pc.playername)
		assertEquals("Test PC", pc.name)

		// basic attribute names are matching.
		assertEquals("Transparent", pc.background.name)
		assertEquals(bg, pc.background)

		assertEquals("Class", pc.klassFirst.name)
		assertEquals(klass, pc.klassFirst)

		assertEquals("Test", pc.race.name)
		assertEquals(race, pc.race)

		// get hitdice: one hitdie by the klass "Klass", faces: 9, level: 1
		val hitdice = pc.hitdice
		log.info("PC hitdice: ${hitdice}")
		assertEquals(1, pc.level)
		assertEquals(pc.level, hitdice.size)
		assertEquals(9, hitdice[0])

		// the first hitpoints are depended on the first klass, full face + CON mod
		log.info("PC hitpoints: ${pc.hitpoints}")
		assertEquals(9 + pc.abilityModifier(Ability.CON), pc.hitpoints)

		// TODO(2020-10-02) test proficiencies
		// TODO(2020-10-02) test ability modifiers
		// TODO(2020-10-02) test skill scores
		// TODO(2020-10-02) test item proficiencies

		log.info("PC XP, Lvl ${pc.experiencePoints}, ${pc.level}")
		assertEquals(0, pc.experiencePoints)
		assertEquals(1, pc.level)

		pc.experiencePoints += 2700 // => level 4
		assertEquals(2700, pc.experiencePoints)
		assertEquals(4, pc.level)

		// add klass levels => gain more traits and HP.
		// TODO (2020-10-02) add tests.

		// TODO (2020-10-02) inventory tests.
		pc.pickupItem(backpack, "BAG:Backpack")
		assertTrue(true, "Backpack is picked up.") // TODO

		pc.pickupItem(spear)
		assertTrue(true, "Spear is hold in hands.") // TODO

		pc.pickupItem(dagger)
		assertTrue(true, "Dagger is hold in hands.") // TODO

		val dagger2 = dagger.copy()
		pc.pickupItem(dagger2)
		assertTrue(true, "Do not hold another dagger.") // TODO

		pc.dropFromHands(handFirst = true)
		assertTrue(true, "Drop spear") // TODO

		pc.pickupItem(dagger2)
		assertTrue(true, "Hold two daggers") // TODO

		// get +60 daggers => inventory +60lb
		for (i in 1..60) pc.pickupItem(dagger.copy(), "BAG:Backpack")
		assertTrue(true, "Picked up 60 daggers, Backpack is now at least 60lb") // TODO

		// drop 10 daggers. => 50 daggers.
		pc.dropFromBag("BAG:Backpack", { index, item -> item == dagger && index < 10 })
		assertTrue(true, "Dropped 10 daggers.") // TODO

		pc.dropFromHands(true)
		assertTrue(true, "Both hands are free now.") // TODO

		pc.pickupItem(pouch.copy(), "BAG:Backpack")
		assertTrue(true, "Picked up pouch and put into backpack") // TODO

		pc.pickupItem(pouch.copy(), "BAG:Backpack")
		assertTrue(true, "Picked up pouch and put into backpack") // TODO

		pc.pickupItem(pouch.copy(), "BAG:Backpack:NESTED Pouch No. 1")
		assertTrue(true, "Picked up pouch and put into pouch which's in the backpack") // TODO

		pc.pickupItem(pouch.copy(), "BAG:Backpack")
		assertTrue(true, "Picked up another pouch and put into backpack, again, not into the pouch") // TODO

		val healing = LooseItem(
			name = "Potion of Healing",
			weight = 0.5,
			cost = Money(gp = 50),
			validContainer = listOf(), // vial, bottle, flask, jug, pot, waterskin
			count = 0.1137, // 4 ounces = 0.1137 l
			measure = LooseItem.Measure.LITER,
			filledDescription = "Vial (4 ounces): 0.5 lb"
			)

		val vial = Container("Vial", 0.0, Money(gp = 1), 0.0, 1, "5 ounces liquid")

		val vialHealing = vial.copy().apply { insert(healing) }

		pc.pickupItem(vialHealing, "BAG:Backpack")

		// TODO (2020-10-02) test spell learning and casting.

		spells.forEach { pc.learnSpell(it, "Wizard") }

		pc.castSpell(spells.find{ it == guidance }!!)
		assertTrue(true, "Casted Guidance => Active & Concentration") // TODO
		assertTrue(true, "Used no Spell slot (Cantrip)") // TODO

		pc.prepareSpell(spells.find{ it.name == "Spell 4" }!!)

		pc.castSpell(spells.find{ it.name == "Spell 4" }!!)
		assertTrue(true, "Casted Spell 4 => (Also) Active") // TODO
		assertTrue(true, "Used a Spell slot") // TODO

		pc.prepareSpell(spells.find{ it.name == "Spell 7" }!!)

		// TODO (2020-10-02) test changing conditions and session stuff.
		// pc.current.conditions += Condition.UNCONSCIOUS to -1
		assertTrue(true, "Character is unconscious") // TODO

		// TODO (2020-10-02) hit with pierce
		// TODO (2020-10-02) hit with bludge
		// TODO (2020-10-02) hit with slash
		// TODO (2020-10-02) hit with fire, etc ...

		// TODO (2020-10-02) consume healing potion

		// TODO (2020-10-02) short rest, use ceil(hitdice.size/2) dice, check available spell slots
		// TODO (2020-10-02) long rest => check available spellslots

		// TODO (2020-10-02) fight the death!
	}

	@Test
	fun testMulticlassing() {
		// TODO (2020-10-02) check if character level are high enough to level up
		// TODO (2020-10-02) check if the second klass can be applied
		// TODO (2020-10-02) check if all feats are applied

		// pc.addKlassLevel(klass)
		// pc.experiencePoints = 3000 // at least level 4
		// pc.addKlassLevel(klass, "Job")
		// pc.addKlassLevel(klass, "Job")
		// pc.addKlassLevel(Klass("Multithing"))
		// pc.addKlassLevel(Klass("Multithing"))
	}

	val mageHand = Spell(
		name = "Mage Hand", // name
		school = "CONJURATION", level = 0, // school, level
		components = "V,S",
		castingTime = "1 action",
		range = "0 ft, TOUCH",
		duration = 60, // 1 minute
		concentration = false, // casting time, range, components, duration, concentration
		ritual = false,
		// attackSave = null, // no attack
		// damageEffect = "", // no effect
		note = """
		Vansishes over 30ft range, or re-cast;
		manipulate objects, open / unlock container, stow / retrive item, pour contents;
		cannot attack, cannot activate magic items, cannot carry more than 10 pounds
		""")

	val guidance = Spell(
		name = "Guidance",
		school = "DIVINATION",
		level = 0,
		components = "V,S",
		castingTime = "1 action",
		range = "0 ft, TOUCH",
		duration = 60, // 1 minute
		concentration = true,
		ritual = false, // is ritual
		// attackSave = null, // no attack
		// damageEffect = "", // no effect
		note = """
		1. Touch a willing creature.
		2. Roll d4, add to one ability check of choice (pre/post). End.
		""".trimIndent() // description
		)

	val spells: List<Spell> = listOf(
		  Spell("Spell 5", "ILLUSION",      5, "1 action", "5 ft  CUBE", "V,M,S",  1,    false, true , "...")
		, Spell("Spell 1", "CONJURATION",   1, "1 action", "0 ft  TOUCH","V,M,S", 60,    false, false, "...")
		, mageHand
		, Spell("Spell 0", "ABJURATION",    0, "1 action", "0 ft  TOUCH","V,M,S", 60,    false, false, "...")
		, Spell("Spell 6", "NECROMANCY",    6, "1 action", "6 ft  CUBE", "V,M,S", 1,     false, false, "...")
		, guidance
		, Spell("Spell 2", "DIVINATION",    2, "1 action", "1 ft  CUBE", "V,M,S", 1,     false, false, "...")
		, Spell("Spell 4", "EVOCATION",     4, "1 action", "4 ft  CUBE", "V,M,S", 86400, false, true , "...")
		, Spell("Spell 7", "TRANSMUTATION", 7, "1 action", "7 ft  CUBE", "V,M,S", 60,    false, false, "...")
		, Spell("Spell 3", "ENCHANTMENT",   3, "1 action", "3 ft  CUBE", "V,M,S", 1,     false, false, "...")
		)

	val backpack = Container(
		name = "Backpack",
		weight = 5.0, cost = Money(gp=2),
		maxWeight = 30.0,
		maxItems = 0,
		capacity = "1 cubic foot/ 30 pounds; also items can be straped to the outside")

	val pouch = Container(
		"Pouch",
		1.0, Money(sp=5),
		6.0, 0, "0.2 cubic foot / 6.0 lb")

	val dagger =  Weapon("Dagger", 1.0, Money(gp=2),
		weightClass = WeightClass.LIGHT,
		weaponType = Weapon.Type.SIMPLE_MELEE,
		damage = DiceTerm(D4),
		damageType = setOf(DamageType.PIERCING),
		throwable = true,
		isFinesse = true,
		note = "Finesse, light, thrown (range 20/60)")

	val spear =  Weapon("Spear", 3.0, Money(gp=1),
		weightClass = WeightClass.NONE,
		weaponType = Weapon.Type.SIMPLE_MELEE,
		damage = DiceTerm(D6),
		damageType = setOf(DamageType.PIERCING),
		throwable = true,
		// versatile: 1d8
		note = "Thrown (range 20/60) | versatile (1d8)")

	val race = Race(
		name = "Test",
		abilityScoreIncrease = mapOf (
			Ability.INT to 2,
			Ability.DEX to 10,
			Ability.STR to -1 ),
		age = mapOf(
			"adult" to 18,
			"dead" to 30,
			"limit" to 100),
		size = Size.GIANT,
		languages = listOf("Test", "Build"), // no common
		darkvision = 10,
		features = listOf(
		),
		subrace = mapOf(
			"Failed" to listOf(Race.Feature("Nope, nope")),
			"Exception" to listOf(Race.Feature("Abort!")),
			"Passed" to listOf(Race.Feature("Ok")))
		)

	val bg = Background(
		name = "Transparent",
		proficiencies = listOf(Skill.ARCANA, Skill.ATHLETICS),
		equipment = listOf(),
		money = Money(gp=0)).apply {
			extraLanguages = 2
		}

	val klass = Klass(
		"Class",
		hitdie = SimpleDice(9), // d9
		savingThrows = listOf(Ability.DEX, Ability.CHA),
		klassLevelTable = setOf(
			Klass.Feature(1,  "Class (1)", "Text: Class (1)"),
			Klass.Feature(4,  "Class (4)", "Text: Class (4)"),
			Klass.Feature(10,  "Class (10)", "Text: Class (10)")
		),
		specialisations = mapOf(
			"Job" to setOf(
				Klass.Feature(1,  "The Job", "Text: The Job"),
				Klass.Feature(3,  "Job #3", "Text: Job #3"),
				Klass.Feature(10,  "Job #10", "Text: Job #10")
			),
			"Occupation" to setOf(
				Klass.Feature(1,  "Occupation, Nr. 1", "Text: Occupation, Nr. 1"),
				Klass.Feature(3,  "Occupation, Nr. 3", "Text: Occupation, Nr. 3"),
				Klass.Feature(5,  "Occupation, Nr. 5", "Text: Occupation, Nr. 5")
			)
		),
		description = "Just the real class."
	)

	val pc = PlayerCharacter.Creator("Nox")
		.race(race, "Failed")
		.background(bg, "Test")
		.klass(klass)
		.name("Test PC")
		.ageInYears(19)
		.awake()
}