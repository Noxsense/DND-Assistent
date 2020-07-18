package de.nox.dndassistant

import kotlin.math.floor

private fun getModifier(value: Int) = floor((value - 10) / 2.0).toInt()

private val logger = LoggerFactory.getLogger("PlayerCharacter")

data class PlayerCharacter(
	val name: String,
	val race: String = "Human" /*TODO*/,
	val gender: String = "divers",
	val player: String = ""
	) {

	var expiriencePoints: Int
		= 0

	var abilityScore: Map<Ability, Int>
		= enumValues<Ability>().map { it }.associateWith { 10 }
		private set

	var abilityModifier: Map<Ability, Int>
		= abilityScore.mapValues { getModifier(it.value) }
		private set

	var savingThrows: List<Ability>
		= listOf()
		private set

	var proficientSkills: Map<Skill, Proficiency>
		= mapOf()
		private set

	var proficientTools: Map<String, Proficiency>
		= mapOf()
		private set

	var proficiencies: Map<Skillable, Proficiency>
		= mapOf()
		private set

	var proficientValue: Int
		= 2

	fun setAbilityScores(xs: Map<Ability,Int>) {
		abilityScore = xs
		setAbilityModifiers()
	}

	private fun setAbilityModifiers() {
		abilityModifier = abilityScore.mapValues { getModifier(it.value) }
	}

	/* Set the attributes' values.
	 * If a param is set to (-1), it will be rolled with a D20.
	 */
	fun setAbilityScore(a: Ability, v: Int) {
		abilityScore += Pair(a, if (v > 0) v else 0)
		abilityModifier = abilityScore.mapValues { getModifier(it.value) }
	}

	fun savingScore(a: Ability) : Int
		= abilityModifier.getOrDefault(a, 0) +
			if (a in savingThrows) proficientValue else 0

	fun skillScore(s: Skill) : Int
		= abilityModifier.getOrDefault(s.source, 0) +
			getProficiencyFor(s).factor * proficientValue

	fun abilityScore(a: Ability) : Int
		= abilityScore.getOrDefault(a, 10)

	fun abilityModifier(a: Ability) : Int
		= abilityModifier.getOrDefault(a, 10)

	fun getProficiencyFor(skill: Skill) : Proficiency
		= proficientSkills.getOrDefault(skill, Proficiency.NONE)

	/* Add proficiency to saving trhow.*/
	fun addProficiency(saving: Ability) {
		if (!(saving in savingThrows)) savingThrows += saving
	}

	/* Add proficiency to a skill. If twice, add expertise.*/
	fun addProficiency(skill: Skill) {
		// increase the proficcient value.
		proficientSkills += Pair(skill, Proficiency.PROFICIENT + proficientSkills[skill])
	}

	val initiative: Int get()
		= this.abilityModifier.getOrDefault(Ability.DEX,
			getModifier(this.abilityScore.getOrDefault(Ability.DEX, 0)))

	var maxHitPoints: Int = -1
	var curHitPoints: Int = -1
	var tmpHitPoints: Int = -1

	val hasTmpHitpoints: Boolean get()
		= tmpHitPoints > 0 && tmpHitPoints != maxHitPoints

	var deathSaves: Array<Int> = arrayOf(0, 0, 0, 0, 0) // maximal 5 chances.
		private set

	fun resetDeathSaves() {
		deathSaves.map { _ -> 0 }
	}

	/* Count fails and successes and returns the more significcant value.*/
	fun checkDeathFight() : Int {
		val success = deathSaves.count { it > 0 }
		val failed = deathSaves.count { it < 0 }

		return when {
			success > 2 -> 3
			failed > 2 -> -3
			success > failed -> 1
			success < failed -> -1
			else -> 0
		}
	}

	/* Take a short rest. Recover hitpoints, maybe magic points, etc. */
	fun rest(shortRest: Boolean = true) {
		println("Rest " + (if (shortRest) "short" else "long"))
		// TODO (2020-06-26)
	}

	var knownLanguages: List<String>
		= listOf("Common")

	var purse: Money = Money()

	var inventory:  List<Item>
		= listOf()
		private set

	/*
	 * Lifting and Carrying (https://dnd.wizards.com/products/tabletop/players-basic-rules#lifting-and-carrying)
	 */

	/* Maximum of the weight, this character could carry. */
	val carryingCapacity: Double get ()
		= abilityScore(Ability.STR) * 15.0

	/* Weight of the inventory and the purse.*/
	val inventoryWeight: Double get()
		= inventory.sumByDouble { it.weight } + purse.weight

	/** Variant: Encumbrance
	 * The rules for lifting and carrying are intentionally simple. Here is a
	 * variant if you are looking for more detailed rules for determining how
	 * a character is hindered by the weight of equipment. When you use this
	 * variant, ignore the Strength column of the Armor table in chapter 5.
	 *
	 * If you carry weight in excess of 5 times your Strength score, you are
	 * encumbered, which means your speed drops by 10 feet.
	 *
	 * If you carry weight in excess of 10 times your Strength score, up to
	 * your maximum carrying capacity, you are instead heavily encumbered, which
	 * means your speed drops by 20 feet and you have disadvantage on ability
	 * checks, attack rolls, and saving throws that use Strength, Dexterity, or
	 * Constitution.
	 */
	fun carryEncumbranceLevel() : Double
		= inventoryWeight / abilityScore(Ability.STR)

	/* Try to buy an item. On success, return true. */
	fun buyItem(i: Item) : Boolean {
		if (purse >= i.cost) {
			purse -= i.cost // give money
			pickupItem(i) // get item
			logger.info("${name} - Bought new ${i} for ${i.cost}, left ${purse}")
			return true
		} else {
			val missed = (purse - i.cost)
			logger.info("${name} - Not enough money to buy. Missed: ${missed}")
			return false
		}
	}

	/* Try to sell an item. On success, return true. */
	fun sellItem(i: Item) : Boolean {
		if (i in inventory) {
			purse += i.cost // earn the money
			dropItem(i) // remove from inventory.
			logger.info("${name} - Sold own ${i} for ${i.cost}, now ${purse}")
			return true
		} else {
			logger.info("${name} - Cannot sell the item (${i}), they do not own this.")
			return false
		}
	}

	fun pickupItem(i: Item) {
		inventory += i
		logger.info("${name} - Picked up ${i}")
	}

	fun dropItem(i: Item) : Boolean {
		// return true on success.
		if (i in inventory) {
			inventory -= (i) // remove from inventory.
			logger.info("${name} - Dropped ${i}")
			return true
		}
		return false
	}

	fun dropItemAt(i: Int) : Item? {
		// return true on success.
		// TODO (2020-07-08) implement
		logger.info("${name} - Dropped ${i}th item")
		return null
	}

	////////////////////////////////////////////////

	val armorClass: Int get() {
		// TODO (2020-06-26)
		// Look up, what the PC's wearing. Maybe add DEX modifier.
		// Not wearing anything: AC 10 + DEX
		return 10 + abilityModifier.getOrDefault(Ability.DEX, 0)
	}

	var equippedArmor: List<Armor> = listOf()
		private set

	/* Equip a piece of armor.
	 * If replace, put the currently worn armor into inventory,
	 * otherwise do not equip.
	 * Only armor, which is in inventory (in possession) can be equipped.*/
	fun wear(a: Armor, replace: Boolean = true) {
		if (a !in inventory) { // abort
			return
		}

		/* Check, if there is already something worn.*/
		val worn = equippedArmor.filter { it.type == a.type }
		val wornSize = worn.size

		/* Human specific. Update for others! TODO */
		var free: Boolean = when (a.type) {
			BodyType.HEAD -> wornSize < 1
			BodyType.SHOULDERS -> wornSize < 1
			BodyType.ACCESSOIRE -> wornSize < 1
			BodyType.HAND -> wornSize < 2
			BodyType.RING -> wornSize < 10
			BodyType.FOOT -> wornSize < 2
		}

		/* Replace the worn equipment, put back to inventory.*/
		if (replace && !free) {
			inventory += worn
			equippedArmor -= worn
			free = true
		}

		if (free) {
			equippedArmor += a
			inventory -= a
		}
	}

	///////////////////////////////////////////////

	var handMain: Weapon? = null
		private set

	var handOff: Weapon? = null
		private set

	fun hand(main: Boolean = true) : Weapon? = when (main) {
		true -> handMain
		false -> handOff
	}

	fun isWieldingAny() : Boolean
		= handMain != null || handOff != null

	/** Check if the hand is wielding a weapon.*/
	fun isWielding(mainHand: Boolean) = when {
		mainHand && handMain != null -> true
		!mainHand && handOff != null -> true
		else -> false
	}

	/* Equip a weapon.
	 * If replace, put the currently wield weapon into inventory,
	 * otherwise do not equip the weapon.
	 * Only weapons, which is in inventory (in possession) can be equipped.*/
	fun wield(w: Weapon, mainHand: Boolean = true, replace: Boolean = false) {
		if (w !in inventory) { // abort
			return
		}

		var success: Boolean = true

		if (replace) unwield(mainHand)

		if (w.isTwoHanded) {
			// needs both hands to be free.
			if (handMain == null && handOff == null) {
				// free hands.
				handMain = w
				handOff = w
			} else if (replace) {
				unwield()
				handMain = w
				handOff = w
			} else {
				success = false
			}
		} else if (mainHand) {
			if (handMain == null) {
				handMain = w // free hand
			} else if (replace) {
				unwield()
				handMain = w
			} else {
				success = false
			}
		} else {
			if (handOff == null) {
				handOff = w // free hand
			} else if (replace) {
				unwield()
				handOff = w
			} else {
				success = false
			}
		}

		if (success) inventory -= w // remove from inventory on success.
	}

	/** Unequip the current weapon(s). */
	fun unwield(mainHand: Boolean = true, both: Boolean = false) {
		if ((mainHand || both) && handMain != null) {
			inventory += handMain!!

			// unequip both hands.
			if (handMain!!.isTwoHanded) {
				handOff = null
			}

			handMain = null
		}

		if ((!mainHand || both) && handOff != null) {
			inventory += handOff!!

			// unequip both hands.
			if (handOff!!.isTwoHanded) {
				handMain = null
			}

			handOff = null
		}
	}
}

/* The base ability score.*/
enum class Ability(val fullname: String) {
	STR("STRENGTH"),
	DEX("DEXTERITY"),
	CON("CONSTITUTION"),
	INT("INTELLIGENCE"),
	WIS("WISDOM"),
	CHA("CHARISMA")
}

enum class BodyType {
	HEAD, // hat, helmet...
	SHOULDERS, // like cloaks ...
	ACCESSOIRE, // like necklace ...
	HAND, // like for one glove (2x)
	RING, // for fingers (10x... species related)
	FOOT, // for one shoe or so (2x)
	// SHIELD // only one // ARM
}
