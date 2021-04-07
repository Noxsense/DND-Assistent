package de.nox.dndassistant.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.math.abs

import java.io.File

import org.json.JSONException
import org.json.JSONTokener
import org.json.JSONArray
import org.json.JSONObject

class JSONLibTest {

	private val log = LoggerFactory.getLogger("Hero-Loader-Test")

	private val testItemCatalog: Map<String, PreSimpleItem> = mapOf (
		"Copper Coin"   to PreSimpleItem("Currency", 0.02, 1, false),
		"Silver Coin"   to PreSimpleItem("Currency", 0.02, SimpleItem.SP_TO_CP, false),
		"Gold Coin"     to PreSimpleItem("Currency", 0.02, SimpleItem.GP_TO_CP, false),
		"Electrum Coin" to PreSimpleItem("Currency", 0.02, SimpleItem.EP_TO_CP, false),
		"Platinum Coin" to PreSimpleItem("Currency", 0.02, SimpleItem.PP_TO_CP, false),

		"Mage Armor Vest" to PreSimpleItem("Clothing",  3.0, SimpleItem.SP_TO_CP * 5, false),

		"Ember Collar" to PreSimpleItem("Artefact",  0.0, 0, false),
		"Ring of Spell Storing" to PreSimpleItem("Ring",  0.0, SimpleItem.GP_TO_CP * 20000, false),
		"Focus (pet collar)" to PreSimpleItem("Arcane Focus",  1.0, 0, false),
		"Potion of Greater Healing" to PreSimpleItem("Potion",  0.0, 0, true),
		"Sword of Answering" to PreSimpleItem("Weapon",  0.0, 0, false),

		"Pouch"  to PreSimpleItem("Adventuring Gear", 1.0, 0, false), // can hold 0.2 ft^3 or 6 lb

		"Ball"   to PreSimpleItem("Miscelleanous", 0.01, 1, false),

		"Dagger" to PreSimpleItem("Simple Meelee Weapon", 1.0, 4, false), // 1d4 piercing, finesse, simple melee, throwable

		"Flask"  to PreSimpleItem("Container", 1.0, 2, false), // can hold 1 pint of liquid (0.00056826125 m^3 = 568.26125 ml)
		"Oil"    to PreSimpleItem("Miscelleanous", 1.0, SimpleItem.SP_TO_CP, false) // 1lb oil is worth 1sp
	)

	// get from testItemCatalog
	private fun Map<String, PreSimpleItem>.getItem(name: String, id: String)
		= this.get(name)?.let { (category, weight, coppers, dividable) -> SimpleItem(
			name = name,
			identifier = id,
			category = category,
			weight = weight,
			copperValue = coppers,
			dividable = dividable,
			)
		}

	private val exampleHero = Hero("Name", "Race" to "Subrace", player = null as String?). apply {
		// val race: Pair<String, String>
		// var name: String
		name = "Re Name"

		// var level = 1
		// val proficiencyBonus: Int
		level = 14

		// var experience: Experience
		experience.points += 200 // prbly to low for lvl 14

		// var player: String?
		player = "Adopting Player"

		// var inspiration: Int
		inspiration += 3 // accumulated as token

		// var speed: MutableMap<String, Int>
		// var walkingSpeed: Int

		// abilities
		// TODO

		// var armorSources: List<String>
		// val armorClass: Int  // depends on feats, spells and equipped clothes
		// val naturalBaseArmorClass: Int // depends on feats and spells

		// var hitpointsMax: Int
		// var hitpointsTmp: Int
		// var hitpointsNow: Int
		// val hitpoints: Pair<Int, Int>  // (current + buffer) to (max + buffer)

		hitpointsMax = 109
		hitpointsTmp = 5 // buffer
		hitpointsNow = 24

		deathsaves.addFailure()
		deathsaves.addSuccess(critical = true)

		// var klasses: MutableList<Triple<String, String?, Int>>

		// var skills: MutableMap<SimpleSkill, Pair<SimpleProficiency, String>>
		// var tools: MutableMap<Pair<String, String>, Pair<SimpleProficiency, String>>

		klasses.plusAssign(Triple("Base Klass", null, 3))
		klasses.plusAssign(Triple("Second Klass", "Chosen Klass Branch", 11))

		// saveProficiences += Ability.WIS
		skills.plusAssign(SimpleSkill.DEFAULT_SKILLS[0] to (SimpleProficiency.P to "Base Klass")) // TODO better syntax
		skills.plusAssign(SimpleSkill("Additional Knitting", Ability.DEX) to (SimpleProficiency.E to "Background")) // TODO better syntax

		tools.plusAssign(("" to "Simple Meelee Weapon") to (SimpleProficiency.P to "Base Klass")) // TODO better syntax
		tools.plusAssign(("Knitting Set" to "") to (SimpleProficiency.P to "Background")) // TODO better syntax

		// val hitdiceMax: Map<String, Int>  // generated by klasses
		// var hitdice: MutableMap<String, Int>
		hitdice = (hitdiceMax - ("d6" to 1)) as MutableMap<String, Int> // use one up? // TODO better syntax.  hitdice -= "d6" => hitdice { "d6": 4, "d4": 1} to { "d6": 3, "d4": 1}

		// var languages: List<String>
		// var specialities: List<Speciality>
		// var conditions: List<Effect>
		languages += "Common"

		specialities += RaceFeature("Race Feature", "Race" to "Subrace")
		specialities += KlassTrait("Klass Trait", Triple("Second Klass", "Chosen Klass Branch", 11))
		specialities += Feat("Chosen Feat")
		specialities += ItemFeature("Feature by atuned or carried Item")
		specialities += CustomCount("Custom Count")

		// - Feat(name: String, count: Count?, description: String)
		// - KlassTrait(name: String, val klass: Triple<String, String, Int>, count: Count?, description: String)
		// - RaceFeature(name: String, val race: Pair<String, String>, val level: Int, count: Count?, description: String)
		// - ItemFeature(name: String, count: Count?, description: String)
		// - CustomCount(name: String, count: Count?, description: String)

		// TODO later human interface: setting klasses or leveling up should add the option to auto add the specialities, without duplicating

		log.debug(conditions)
		// - Prone / grapelled
		// - under spell influence
		// - Effect(name: String, seconds: Int, val removable: Boolean, description: String)

		// var spells: Map<Pair<SimpleSpell, String>, Boolean>
		// val maxPreparedSpells: List<Int>  // depends on klasses, race and feats
		// val spellsPrepared: Set<Pair<SimpleSpell, String>>  // depends on spell
		log.debug(spells)

		// var inventory: MutableList<Pair<SimpleItem, String>>
		inventory.plusAssign(testItemCatalog.getItem("Dagger", "Dagger@0")!! to "")
		inventory.plusAssign(SimpleItem("Backpack", "Backpack@0", "Adventuring Gear", 1.0, 0, false) to "")

		inventory.plusAssign(testItemCatalog.getItem("Sword of Answering", "SoA@0")!! to "Backpack@0")

		inventory.plusAssign(testItemCatalog.getItem("Pouch", "Pouch@0")!! to "Backpack@0")
		(0 .. 5).forEach {
			inventory.plusAssign(testItemCatalog.getItem("Silver Coin", "SP@$it")!! to "Pouch@0")
		}

		inventory.plusAssign(testItemCatalog.getItem("Gold Coin", "GP@0")!! to "Pouch@0")

		inventory.plusAssign(testItemCatalog.getItem("Pouch", "Pouch@1")!! to "Backpack@0")
		(1 .. 3).forEach {
			inventory.plusAssign(testItemCatalog.getItem("Gold Coin", "GP@$it")!! to "Pouch@1")
		}

		inventory.plusAssign(testItemCatalog.getItem("Pouch", "Pouch@Empty")!! to "Backpack@0")

		log.debug(inventory)
	}

	@Test
	fun testLoading() {
		log.info("Test: testLoading()")
		log.displayLevel(LoggingLevel.DEBUG)
		var hero = loadHero(File("src/test/resources/Hero.json").getAbsolutePath())

		log.info("Loaded the Hero: $hero")

		log.info("Test outsider mapping? Better not? (if it was applied, all booleans will be flipped.)")


		log.info("Loaded the Hero: Speed: ${hero.speed.toList().joinToString()}")

		log.info("Loaded the Hero: Klasses: ${hero.klasses}")

		log.info("-".repeat(50))

		log.info("Hero overview")

		"Hero's Name"
			.assertEquals("Example Hero", hero.name)

		"Hero's Player"
			.assertEquals("Nox", hero.player)

		"Hero's Inspiration"
			.assertEquals(2, hero.inspiration) // accumulated inspiration "points" / tokens

		"Hero's Race"
			.assertEquals("Dog" to "Bantam Dog", hero.race)

		"Hero's Level"
			.assertEquals(14, hero.level)

		"Hero's Experience"
			.assertEquals(Hero.Experience(0, "milestone"), hero.experience)

		"Hero's HP"
			.assertEquals(25 to  109, hero.hitpoints)

		"Hero's Death Saves"
			.assertEquals(Hero.DeathSaveFight(), hero.deathsaves) // empty

		"Hero's Death Saves: Is not dead, is not saved (undecided)."
			.assertEquals(null, hero.deathsaves.evalSaved())

		// "Hero's Hit Dice".assertEquals(null, hero.hitdice) // 6d6
		// "Hero's Hit Dice (Max)".assertEquals(14, hero.hitdiceMax.size) // 14 dice: 12 sorcerer + 2 monk

		hero.hitpointsNow -= hero.hitpointsMax*3 // instant death

		"Hero's HP (after deadly blow)"
			.assertEquals(0 to 109, hero.hitpoints)

		"Hero's Death Saves (after deadly blow): Is finally dead."
			.assertEquals(false, hero.deathsaves.evalSaved())

		 // 7 (days in campaign)
		"Hero's Custom Counter Count Before: ${hero.specialities.last()}"
			.assertEquals(6, hero.specialities.last().count?.current)

		hero.specialities.last().countUp()
		"Hero's Custom Counter Count (+1): ${hero.specialities.last()}"
			.assertEquals(7, hero.specialities.last().count?.current)

		hero.specialities.last().countUp(3)
		"Hero's Custom Counter Count (+3): ${hero.specialities.last()}"
			.assertEquals(10, hero.specialities.last().count?.current)

		// TODO (2021-03-23) test hero's langues
		val expectedLangs = listOf("Common", "Canine", "Giant", "Celestial", "Elvish")
		"Hero's Languages: ${hero.languages}"
			.assertEquals(expectedLangs, hero.languages)

		// TODO (2021-03-23) test hero's skill set
		"Hero's Skills: ${hero.skillValues.toList()}}"
			.assertEquals(true, true)

		// TODO (2021-03-23) test hero's klasses (and hitdice)
		"Hero's Classes: ${hero.klasses.toList()}}"
			.assertEquals(true, true)

		// TODO (2021-03-23) test hero's specialities
		"Hero's Specialities: ${hero.specialities}"
			.assertEquals(true, true)

		// TODO (2021-03-23) test hero's attacks
		"Hero's Attacks"
			.assertEquals(true, true)
		//  - [design question:?] physical attacks
		//  - [design question:?] spells
		//  - [design question:?] spell DCs, spell (attack) modifiers, spell level

		// TODO (2021-03-23) test hero's spells
		"Hero's Spells"
			.assertEquals(true, true)
		// [design question] ? - Spells sorted by level?
		// [design question] ? - Also show preparable spells (for later planning?)
		// * - Arcane Focus: ${hero.getArcaneFocus()?.name}
		// * - Spells Prepared: ${hero.spellsPrepared.size} items
		// * - ${hero.spells}

		// TODO (2021-03-23) test hero's conditions
		"Hero's Conditions (Buffs and Effects)".
			assertEquals(true, true)
		// [design question / idea] - being near paladin: +2 saving throws
		// [design question / idea] - being bewitched with Bless: 1d4 on saving throws and co.
		// [design question / idea] - being bewitched with Bane: -1d4 on saving throws
		// [design question / idea] - being bewitched with Mage Armor and naked: 13 + DEX
		// [design question / idea] - being bewitched by Charm: Doing stuff as they like
		// [design question / idea] - being bewitched by Confusing: Doing random stuff
		// [design question / idea] - exhaustion points: 1

		// "Hero's Max Carrying Capacity: STR * 15 = 7 * 15"
		// 	.assertEquals(hero.ability(Ability.STR) * 15.0, hero.maxCarriageWeight())

		// "Hero's Inventory: Count of items"
		// 	.assertEquals(13 + 1 /*oil*/ + 2500 /*gp*/, hero.inventory.size)

		// "Hero's Inventory: Summed weight (more than 2.5k GP (0.01 lb)"
		// 	.assertEquals(true, 25.0 <= hero.inventory.weight())

		// "Hero's Inventory: Summed value (more than 2.5k GP (50 cp)"
		// 	.assertEquals(true, 2500 * SimpleItem.GP_TO_CP <= hero.inventory.copperValue())

		// TODO (2021-03-23) test
		// Inventory:
		// - summed items: ${hero.inventory.size}
		// - summed weight: ${hero.inventory.weight()} / ${maxCarriageWeight()} lb
		// - summed value: ${hero.inventory.copperValue()} cp
		// > DEBUG INVENTORY: $inventory

		log.info("Test: testLoading: OK!")
	}

	// save and check if the re-loaded hero equals contextual the saved.
	@Test
	fun testStoreRestoreJSON() {
		log.info("Test.testStoreRestoreJSON()")
		log.displayLevel(LoggingLevel.DEBUG)

		// Example Hero to JSON
		val heroJSON = exampleHero.toJSON()

		log.debug("Example Hero to Hero.json:\n" + heroJSON)

		// write to file.
		File("/tmp/HeroJSON.json").writeText(heroJSON)

		// load SimpleItem.Catalog for loading equipped SimpleItem
		SimpleItem.Catalog = testItemCatalog + mapOf(
			// custom items.
			"Backpack" to PreSimpleItem("Adventuring Gear", 1.0, 0, false),
		)

		// heroJSON resored to a (new) Hero.
		val restoredHero = Hero.fromJSON(heroJSON)

		log.debug("Example Hero.json to Hero (restored):\n" + restoredHero)

		log.debug("Compare Restored Hero with Example Hero. Should be the same at the state of storing.")

		"Hero's race"
			.assertEquals(exampleHero.race, restoredHero.race)

		"Hero's name"
			.assertEquals(exampleHero.name, restoredHero.name)

		"Hero's level"
			.assertEquals(exampleHero.level, restoredHero.level)

		"Hero's proficiencyBonus"
			.assertEquals(exampleHero.proficiencyBonus, restoredHero.proficiencyBonus)

		"Hero's experience"
			.assertEquals(exampleHero.experience, restoredHero.experience)

		"Hero's player"
			.assertEquals(exampleHero.player, restoredHero.player)

		"Hero's inspiration"
			.assertEquals(exampleHero.inspiration, restoredHero.inspiration)

		"Hero's speed"
			.assertEquals(exampleHero.speed, restoredHero.speed)

		"Hero's walkingSpeed"
			.assertEquals(exampleHero.walkingSpeed, restoredHero.walkingSpeed)

		"Hero's armorSources"
			// .assertEquals(exampleHero.armorSources, restoredHero.armorSources)

		"Hero's armorClass"
			.assertEquals(exampleHero.armorClass, restoredHero.armorClass)

		"Hero's naturalBaseArmorClass"
			.assertEquals(exampleHero.naturalBaseArmorClass, restoredHero.naturalBaseArmorClass)

		"Hero's hitpoints"
			.assertEquals(exampleHero.hitpoints, restoredHero.hitpoints)

		"Hero's hitpointsMax"
			.assertEquals(exampleHero.hitpointsMax, restoredHero.hitpointsMax)

		"Hero's hitpointsTmp"
			.assertEquals(exampleHero.hitpointsTmp, restoredHero.hitpointsTmp)

		"Hero's hitpointsNow"
			.assertEquals(exampleHero.hitpointsNow, restoredHero.hitpointsNow)

		"Hero's deathsaves"
			.assertEquals(exampleHero.deathsaves, restoredHero.deathsaves)

		"Hero's abilities"
			.assertEquals(exampleHero.abilities, restoredHero.abilities)

		"Hero's skills"
			.assertEquals(exampleHero.skills, restoredHero.skills)

		"Hero's skillValues"
			.assertEquals(exampleHero.skillValues, restoredHero.skillValues)

		"Hero's tools"
			.assertEquals(exampleHero.tools, restoredHero.tools)

		"Hero's klasses"
			.assertEquals(exampleHero.klasses, restoredHero.klasses)

		"Hero's hitdiceMax"
			.assertEquals(exampleHero.hitdiceMax, restoredHero.hitdiceMax)

		"Hero's hitdice"
			.assertEquals(exampleHero.hitdice, restoredHero.hitdice)

		"Hero's languages"
			.assertEquals(exampleHero.languages, restoredHero.languages)

		// TODO (comparing test, comparable Speciality Lists.)
		// "Hero's specialities".assertSameByContent(exampleHero.specialities, restoredHero.specialities)

		"Hero's conditions"
			.assertEquals(exampleHero.conditions, restoredHero.conditions)

		"Hero's spellsPrepared"
			.assertEquals(exampleHero.spellsPrepared, restoredHero.spellsPrepared)

		"Hero's maxPreparedSpells"
			.assertEquals(exampleHero.maxPreparedSpells, restoredHero.maxPreparedSpells)

		// TODO (comparing test, comparable <Simple Spells, String> List.)
		"Hero's spells"
			.assertEquals(exampleHero.spells, restoredHero.spells)

		// TODO (comparing test, comparable <SimpleItem, String> List.)
		"Hero's inventory".assertEqualsBy(exampleHero.inventory, restoredHero.inventory) { expected, restored ->
			val a = expected.sortedBy { it.second }
			val b = restored.sortedBy { it.second }

			a.all { it in b } && b.all { it in a }
		}

		log.info("Test: testLoading: OK!")
	}

	@Test
	fun testLoadCatalog() {
		log.info("Test.testLoadCatalog() / loadSimpleItemCatalog")
		var filepath = File("src/test/resources/ItemCatalog.json").getAbsolutePath()
		val txt = readText(filepath)

		// start with an empty SimpleItem.Catalog.
		SimpleItem.Catalog = mapOf()

		"Start with empty SimpleItem.Catalog"
			.assertEquals(0, SimpleItem.Catalog.size)

		// load from file.
		val catalog = loadSimpleItemCatalog(txt)

		"Loaded File successfully"
			.assertEquals(true, catalog.size > 0)

		"Loaded also into SimpleItem.Catalog (greater than 0)"
			.assertEquals(true, SimpleItem.Catalog.size > 0)

		"All file loaded catalog items are now in SimpleItem.Catalog"
			.assertEquals(true, SimpleItem.Catalog.keys.containsAll(catalog.keys))

		log.info("Test.testLoadCatalog() DONE")
	}

	@Test
	fun loadSimpleSpells() {
		log.info("Test.loadSimpleSpells() / SimpleSpell.toJSON(), SimpleSpell.Companion.fromJSON()")

		val spells = listOf(
			SimpleSpell(name = "Simple Spell",                     school = "???", castingTime = "1 act", ritual = false, components = SimpleSpell.Components.VSM(listOf("Piece of cured Leather" to 0)),       range = "Touch",               duration = "Instantaneous", concentration = false, description = "?", levels = mapOf(1 to mapOf()), optAttackRoll = false, optSpellDC = false),
			SimpleSpell(name = "Concentration Spell",              school = "???", castingTime = "1 act", ritual = false, components = SimpleSpell.Components.VSM(listOf("Small Block of Granite" to 0)),       range = "Touch",               duration = "10 min",        concentration =  true, description = "?", levels = mapOf(5 to mapOf()), optAttackRoll = false, optSpellDC = false),
			SimpleSpell(name = "Levelling Spell",                  school = "???", castingTime = "1 act", ritual = false, components = SimpleSpell.Components.VS,                                               range = "Touch",               duration = "Instantaneous", concentration = false, description = "?", levels = (6 .. 9).map { l -> l to mapOf("Heal" to "${(l + 1)*10} hp")}.toMap(), optAttackRoll = false, optSpellDC = false),
			SimpleSpell(name = "Levelling Cantrip",                school = "???", castingTime = "1 act", ritual = false, components = SimpleSpell.Components.VS,                                               range = "120 ft",              duration = "Instantaneous", concentration = false, description = "?", levels = mapOf(0 to mapOf("Attack-Damage" to "1d10 (fire)"), -5 to mapOf("Attack-Damage" to "2d10 (fire)"), -11 to mapOf("Attack-Damage" to "3d10 (fire)"), -17 to mapOf("Attack-Damage" to "4d10 (fire)")), optAttackRoll = true, optSpellDC = false),
			SimpleSpell(name = "Ritual Spell",                     school = "???", castingTime = "1 act", ritual =  true, components = SimpleSpell.Components.VS,                                               range = "self + globe (30ft)", duration = "10 min",        concentration = false, description = "?", levels = mapOf(1 to mapOf()), optAttackRoll = false, optSpellDC = false),
			SimpleSpell(name = "Spell with Materials (GP) Needed", school = "???", castingTime = "1 h",   ritual = false, components = SimpleSpell.Components.VSM(listOf("Diamond" to 1000, "Vessel" to 2000)), range = "Touch",               duration = "Instantaneous", concentration = false, description = "?", levels = mapOf(8 to mapOf()), optAttackRoll = false, optSpellDC = false),
		)

		val spellsJSON = spells.toJSON()

		File("/tmp/Spells.json").writeText(spellsJSON) // XXX

		val spellsFromJSON = (JSONTokener(spellsJSON).nextValue() as JSONArray).let { array ->
			(0 until array.length()).map { i -> SimpleSpell.fromJSON(array.getJSONObject(i).toString()) }
		}

		// to JSON and back.
		assertTrue(spellsFromJSON.containsAll(spells))
		assertTrue(spells.containsAll(spellsFromJSON))


		log.info("Test.loadSimpleSpells() DONE")
	}

	private fun <T> String.assertEquals(expected: T, actual: T)
		= let {
			assertEquals(expected, actual, this)
			log.debug("Assert Equals: '$this' as expected ($expected).")
		}

	private fun <T> String.assertEqualsBy(expected: T, actual: T, equalFun: ((T, T) -> Boolean))
		= let {
			assertTrue(equalFun.invoke(expected, actual), this)
			log.debug("Assert Equals By: '$this' as expected ($expected).")
		}

	// compare by containing all objects
	private fun <T> String.assertSameByContent(expectedCollection: Collection<T>, actualCollection: Collection<T>)
		= let {
			val containsAllActual = expectedCollection.containsAll(actualCollection)
			val containsNotMoreThanActual = actualCollection.contains(expectedCollection)

			log.debug("Expected List: $expectedCollection")
			log.debug("Actual List:   $actualCollection")

			if (!containsAllActual) {
				fail("Expected does not contain all expected => Missing: ${ expectedCollection - actualCollection }")
			}

			if (!containsNotMoreThanActual) {
				fail("Expected contains more than expected => Too Much: ${ actualCollection - actualCollection }")
			}

			log.debug("Assert Same by Content: '$this' as expected ($expectedCollection).")
		}
}
