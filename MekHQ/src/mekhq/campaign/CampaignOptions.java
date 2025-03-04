/*
 * Copyright (c) 2009 - Jay Lawson (jaylawson39 at yahoo.com). All Rights Reserved.
 * Copyright (c) 2020-2022 - The MegaMek Team. All Rights Reserved.
 *
 * This file is part of MekHQ.
 *
 * MekHQ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MekHQ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MekHQ. If not, see <http://www.gnu.org/licenses/>.
 */
package mekhq.campaign;

import megamek.Version;
import megamek.common.EquipmentType;
import megamek.common.TechConstants;
import mekhq.MekHQ;
import mekhq.utilities.MHQXMLUtility;
import mekhq.Utilities;
import mekhq.campaign.enums.PlanetaryAcquisitionFactionLimit;
import mekhq.campaign.finances.Money;
import mekhq.campaign.finances.enums.FinancialYearDuration;
import mekhq.campaign.market.PersonnelMarket;
import mekhq.campaign.market.enums.ContractMarketMethod;
import mekhq.campaign.market.enums.UnitMarketMethod;
import mekhq.campaign.mission.enums.AtBLanceRole;
import mekhq.campaign.parts.enums.PartRepairType;
import mekhq.campaign.personnel.SkillType;
import mekhq.campaign.personnel.enums.*;
import mekhq.campaign.rating.UnitRatingMethod;
import mekhq.service.MassRepairOption;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.PrintWriter;
import java.util.*;
import java.util.Map.Entry;

/**
 * @author natit
 */
public class CampaignOptions {
    //region Variable Declarations
    //region Magic Numbers and Constants
    public static final int TECH_INTRO = 0;
    public static final int TECH_STANDARD = 1;
    public static final int TECH_ADVANCED = 2;
    public static final int TECH_EXPERIMENTAL = 3;
    public static final int TECH_UNOFFICIAL = 4;
    // This must always be the highest tech level in order to hide parts
    // that haven't been invented yet, or that are completely extinct
    public static final int TECH_UNKNOWN = 5;

    public static final int TRANSIT_UNIT_DAY = 0;
    public static final int TRANSIT_UNIT_WEEK = 1;
    public static final int TRANSIT_UNIT_MONTH = 2;
    public static final int TRANSIT_UNIT_NUM = 3;

    public static final String S_TECH = "Tech";
    public static final String S_AUTO = "Automatic Success";

    public static final double MAXIMUM_COMBAT_EQUIPMENT_PERCENT = 5.0;
    public static final double MAXIMUM_DROPSHIP_EQUIPMENT_PERCENT = 1.0;
    public static final double MAXIMUM_JUMPSHIP_EQUIPMENT_PERCENT = 1.0;
    public static final double MAXIMUM_WARSHIP_EQUIPMENT_PERCENT = 1.0;
    //endregion Magic Numbers and Constants

    //region Unlisted Variables
    //Mass Repair/Salvage Options
    private boolean massRepairUseRepair;
    private boolean massRepairUseSalvage;
    private boolean massRepairUseExtraTime;
    private boolean massRepairUseRushJob;
    private boolean massRepairAllowCarryover;
    private boolean massRepairOptimizeToCompleteToday;
    private boolean massRepairScrapImpossible;
    private boolean massRepairUseAssignedTechsFirst;
    private boolean massRepairReplacePod;
    private List<MassRepairOption> massRepairOptions;
    //endregion Unlisted Variables

    //region General Tab
    private UnitRatingMethod unitRatingMethod;
    private int manualUnitRatingModifier;
    //endregion General Tab

    //region Repair and Maintenance Tab
    // Repair
    private boolean useEraMods;
    private boolean assignedTechFirst;
    private boolean resetToFirstTech;
    private boolean useQuirks;
    private boolean useAeroSystemHits;
    private boolean destroyByMargin;
    private int destroyMargin;
    private int destroyPartTarget;

    // Maintenance
    private boolean checkMaintenance;
    private int maintenanceCycleDays;
    private int maintenanceBonus;
    private boolean useQualityMaintenance;
    private boolean reverseQualityNames;
    private boolean useUnofficialMaintenance;
    private boolean logMaintenance;
    //endregion Repair and Maintenance Tab

    //region Supplies and Acquisition Tab
    // Acquisition
    private int waitingPeriod;
    private String acquisitionSkill;
    private boolean acquisitionSupportStaffOnly;
    private int clanAcquisitionPenalty;
    private int isAcquisitionPenalty;
    private int maxAcquisitions;

    // Delivery
    private int nDiceTransitTime;
    private int constantTransitTime;
    private int unitTransitTime;
    private int acquireMinimumTime;
    private int acquireMinimumTimeUnit;
    private int acquireMosBonus;
    private int acquireMosUnit;

    // Planetary Acquisition
    private boolean usePlanetaryAcquisition;
    private int maxJumpsPlanetaryAcquisition;
    private PlanetaryAcquisitionFactionLimit planetAcquisitionFactionLimit;
    private boolean planetAcquisitionNoClanCrossover;
    private boolean noClanPartsFromIS;
    private int penaltyClanPartsFromIS;
    private boolean planetAcquisitionVerbose;
    private int[] planetTechAcquisitionBonus;
    private int[] planetIndustryAcquisitionBonus;
    private int[] planetOutputAcquisitionBonus;
    //endregion Supplies and Acquisition Tab

    //region Tech Limits Tab
    private boolean limitByYear;
    private boolean disallowExtinctStuff;
    private boolean allowClanPurchases;
    private boolean allowISPurchases;
    private boolean allowCanonOnly;
    private boolean allowCanonRefitOnly;
    private int techLevel;
    private boolean variableTechLevel;
    private boolean factionIntroDate;
    private boolean useAmmoByType; // Unofficial
    //endregion Tech Limits Tab

    //region Personnel Tab
    // General Personnel
    private boolean useTactics;
    private boolean useInitiativeBonus;
    private boolean useToughness;
    private boolean useArtillery;
    private boolean useAbilities;
    private boolean useEdge;
    private boolean useSupportEdge;
    private boolean useImplants;
    private boolean alternativeQualityAveraging;
    private boolean useTransfers;
    private boolean useExtendedTOEForceName;
    private boolean personnelLogSkillGain;
    private boolean personnelLogAbilityGain;
    private boolean personnelLogEdgeGain;

    // Expanded Personnel Information
    private boolean useTimeInService;
    private TimeInDisplayFormat timeInServiceDisplayFormat;
    private boolean useTimeInRank;
    private TimeInDisplayFormat timeInRankDisplayFormat;
    private boolean trackTotalEarnings;
    private boolean trackTotalXPEarnings;
    private boolean showOriginFaction;

    // Medical
    private boolean useAdvancedMedical; // Unofficial
    private int healWaitingPeriod;
    private int naturalHealingWaitingPeriod;
    private int minimumHitsForVehicles;
    private boolean useRandomHitsForVehicles;
    private boolean tougherHealing;

    // Prisoners
    private PrisonerCaptureStyle prisonerCaptureStyle;
    private PrisonerStatus defaultPrisonerStatus;
    private boolean prisonerBabyStatus;
    private boolean useAtBPrisonerDefection;
    private boolean useAtBPrisonerRansom;

    // Personnel Randomization
    private boolean useDylansRandomXP; // Unofficial
    private RandomOriginOptions randomOriginOptions;

    // Retirement
    private boolean useRetirementDateTracking;
    private RandomRetirementMethod randomRetirementMethod;
    private boolean useYearEndRandomRetirement;
    private boolean useContractCompletionRandomRetirement;
    private boolean useCustomRetirementModifiers;
    private boolean useRandomFounderRetirement;
    private boolean trackUnitFatigue;

    // Family
    private FamilialRelationshipDisplayLevel displayFamilyLevel;

    // Dependent
    private RandomDependentMethod randomDependentMethod;
    private boolean useRandomDependentAddition;
    private boolean useRandomDependentRemoval;

    // Salary
    private double salaryCommissionMultiplier;
    private double salaryEnlistedMultiplier;
    private double salaryAntiMekMultiplier;
    private double salarySpecialistInfantryMultiplier;
    private double[] salaryXPMultipliers;
    private Money[] roleBaseSalaries;

    // Marriage
    private boolean useManualMarriages;
    private boolean useClannerMarriages;
    private boolean usePrisonerMarriages;
    private int minimumMarriageAge;
    private int checkMutualAncestorsDepth;
    private boolean logMarriageNameChanges;
    private Map<MergingSurnameStyle, Integer> marriageSurnameWeights;
    private RandomMarriageMethod randomMarriageMethod;
    private boolean useRandomSameSexMarriages;
    private boolean useRandomClannerMarriages;
    private boolean useRandomPrisonerMarriages;
    private int randomMarriageAgeRange;
    private double percentageRandomMarriageOppositeSexChance;
    private double percentageRandomMarriageSameSexChance;

    // Divorce
    private boolean useManualDivorce;
    private boolean useClannerDivorce;
    private boolean usePrisonerDivorce;
    private Map<SplittingSurnameStyle, Integer> divorceSurnameWeights;
    private RandomDivorceMethod randomDivorceMethod;
    private boolean useRandomOppositeSexDivorce;
    private boolean useRandomSameSexDivorce;
    private boolean useRandomClannerDivorce;
    private boolean useRandomPrisonerDivorce;
    private double percentageRandomDivorceOppositeSexChance;
    private double percentageRandomDivorceSameSexChance;

    // Procreation
    private boolean useManualProcreation;
    private boolean useClannerProcreation;
    private boolean usePrisonerProcreation;
    private int multiplePregnancyOccurrences;
    private BabySurnameStyle babySurnameStyle;
    private boolean assignNonPrisonerBabiesFounderTag;
    private boolean assignChildrenOfFoundersFounderTag;
    private boolean determineFatherAtBirth;
    private boolean displayTrueDueDate;
    private boolean logProcreation;
    private RandomProcreationMethod randomProcreationMethod;
    private boolean useRelationshiplessRandomProcreation;
    private boolean useRandomClannerProcreation;
    private boolean useRandomPrisonerProcreation;
    private double percentageRandomProcreationRelationshipChance;
    private double percentageRandomProcreationRelationshiplessChance;

    // Death
    private boolean keepMarriedNameUponSpouseDeath;
    private RandomDeathMethod randomDeathMethod;
    private Map<AgeGroup, Boolean> enabledRandomDeathAgeGroups;
    private boolean useRandomClanPersonnelDeath;
    private boolean useRandomPrisonerDeath;
    private boolean useRandomDeathSuicideCause;
    private double percentageRandomDeathChance;
    private double[] exponentialRandomDeathMaleValues;
    private double[] exponentialRandomDeathFemaleValues;
    private Map<TenYearAgeRange, Double> ageRangeRandomDeathMaleValues;
    private Map<TenYearAgeRange, Double> ageRangeRandomDeathFemaleValues;
    //endregion Personnel Tab

    //region Finance tab
    private boolean payForParts;
    private boolean payForRepairs;
    private boolean payForUnits;
    private boolean payForSalaries;
    private boolean payForOverhead;
    private boolean payForMaintain;
    private boolean payForTransport;
    private boolean sellUnits;
    private boolean sellParts;
    private boolean payForRecruitment;
    private boolean useLoanLimits;
    private boolean usePercentageMaint; // Unofficial
    private boolean infantryDontCount; // Unofficial
    private boolean usePeacetimeCost;
    private boolean useExtendedPartsModifier;
    private boolean showPeacetimeCost;
    private FinancialYearDuration financialYearDuration;
    private boolean newFinancialYearFinancesToCSVExport;

    // Price Multipliers
    private double commonPartPriceMultiplier;
    private double innerSphereUnitPriceMultiplier;
    private double innerSpherePartPriceMultiplier;
    private double clanUnitPriceMultiplier;
    private double clanPartPriceMultiplier;
    private double mixedTechUnitPriceMultiplier;
    private double[] usedPartPriceMultipliers;
    private double damagedPartsValueMultiplier;
    private double unrepairablePartsValueMultiplier;
    private double cancelledOrderRefundMultiplier;
    //endregion Finance Tab

    //region Mercenary Tab
    private boolean equipmentContractBase;
    private double equipmentContractPercent;
    private boolean equipmentContractSaleValue;
    private double dropshipContractPercent;
    private double jumpshipContractPercent;
    private double warshipContractPercent;
    private boolean blcSaleValue;
    private boolean overageRepaymentInFinalPayment;
    //endregion Mercenary Tab

    //region Experience Tab
    private int scenarioXP;
    private int killXPAward;
    private int killsForXP;
    private int tasksXP;
    private int nTasksXP;
    private int successXP;
    private int mistakeXP;
    private int idleXP;
    private int monthsIdleXP;
    private int targetIdleXP;
    private int contractNegotiationXP;
    private int adminXP;
    private int adminXPPeriod;
    private int edgeCost;
    //endregion Experience Tab

    //region Skills Tab
    //endregion Skills Tab

    //region Special Abilities Tab
    //endregion Special Abilities Tab

    //region Skill Randomization Tab
    private int[] phenotypeProbabilities;
    //endregion Skill Randomization Tab

    //region Rank System Tab
    //endregion Rank System Tab

    //region Name and Portrait Generation
    private boolean useOriginFactionForNames;
    private boolean[] usePortraitForRole;
    private boolean assignPortraitOnRoleChange;
    //endregion Name and Portrait Generation

    //region Markets Tab
    // Personnel Market
    private String personnelMarketName;
    private boolean personnelMarketReportRefresh;
    private int personnelMarketRandomEliteRemoval;
    private int personnelMarketRandomVeteranRemoval;
    private int personnelMarketRandomRegularRemoval;
    private int personnelMarketRandomGreenRemoval;
    private int personnelMarketRandomUltraGreenRemoval;
    private double personnelMarketDylansWeight;

    // Unit Market
    private UnitMarketMethod unitMarketMethod;
    private boolean unitMarketRegionalMechVariations;
    private boolean instantUnitMarketDelivery;
    private boolean unitMarketReportRefresh;

    // Contract Market
    private ContractMarketMethod contractMarketMethod;
    private boolean contractMarketReportRefresh;
    //endregion Markets Tab

    //region RATs Tab
    private boolean useStaticRATs;
    private String[] rats;
    private boolean ignoreRATEra;
    //endregion RATs Tab

    //region Against the Bot Tab
    private boolean useAtB;
    private boolean useStratCon;
    private int skillLevel;

    // Unit Administration
    private boolean useShareSystem;
    private boolean sharesExcludeLargeCraft;
    private boolean sharesForAll;
    private boolean aeroRecruitsHaveUnits;
    private boolean useLeadership;
    private boolean trackOriginalUnit;
    private boolean useAero;
    private boolean useVehicles;
    private boolean clanVehicles;

    // Contract Operations
    private int searchRadius;
    private boolean variableContractLength;
    private boolean mercSizeLimited;
    private boolean restrictPartsByMission;
    private boolean limitLanceWeight;
    private boolean limitLanceNumUnits;
    private boolean useStrategy;
    private int baseStrategyDeployment;
    private int additionalStrategyDeployment;
    private boolean adjustPaymentForStrategy;
    private int[] atbBattleChance;
    private boolean generateChases;

    // Scenarios
    private boolean doubleVehicles;
    private int opforLanceTypeMechs;
    private int opforLanceTypeMixed;
    private int opforLanceTypeVehicles;
    private boolean opforUsesVTOLs;
    private boolean allowOpforAeros;
    private int opforAeroChance;
    private boolean allowOpforLocalUnits;
    private int opforLocalUnitChance;
    private boolean adjustPlayerVehicles;
    private boolean regionalMechVariations;
    private boolean attachedPlayerCamouflage;
    private boolean playerControlsAttachedUnits;
    private boolean useDropShips;
    private boolean useWeatherConditions;
    private boolean useLightConditions;
    private boolean usePlanetaryConditions;
    private int fixedMapChance;
    private int spaUpgradeIntensity;
    //endregion Against the Bot Tab
    //endregion Variable Declarations

    //region Constructors
    public CampaignOptions() {
        // Initialize any reused variables
        final PersonnelRole[] personnelRoles = PersonnelRole.values();

        //region Unlisted Variables
        //Mass Repair/Salvage Options
        massRepairUseRepair = true;
        massRepairUseSalvage = true;
        massRepairUseExtraTime = true;
        massRepairUseRushJob = true;
        massRepairAllowCarryover = true;
        massRepairOptimizeToCompleteToday = false;
        massRepairScrapImpossible = false;
        massRepairUseAssignedTechsFirst = false;
        massRepairReplacePod = true;
        massRepairOptions = new ArrayList<>();

        for (PartRepairType type : PartRepairType.values()) {
            massRepairOptions.add(new MassRepairOption(type));
        }
        //endregion Unlisted Variables

        //region General Tab
        unitRatingMethod = UnitRatingMethod.CAMPAIGN_OPS;
        manualUnitRatingModifier = 0;
        //endregion General Tab

        //region Repair and Maintenance Tab
        // Repair
        useEraMods = false;
        assignedTechFirst = false;
        resetToFirstTech = false;
        useQuirks = false;
        useAeroSystemHits = false;
        destroyByMargin = false;
        destroyMargin = 4;
        destroyPartTarget = 10;

        // Maintenance
        checkMaintenance = true;
        maintenanceCycleDays = 7;
        maintenanceBonus = -1;
        useQualityMaintenance = true;
        reverseQualityNames = false;
        useUnofficialMaintenance = false;
        logMaintenance = false;
        //endregion Repair and Maintenance Tab

        //region Supplies and Acquisitions Tab
        // Acquisition
        waitingPeriod = 7;
        acquisitionSkill = S_TECH;
        acquisitionSupportStaffOnly = true;
        clanAcquisitionPenalty = 0;
        isAcquisitionPenalty = 0;
        maxAcquisitions = 0;

        // Delivery
        nDiceTransitTime = 1;
        constantTransitTime = 0;
        unitTransitTime = TRANSIT_UNIT_MONTH;
        acquireMinimumTime = 1;
        acquireMinimumTimeUnit = TRANSIT_UNIT_MONTH;
        acquireMosBonus = 1;
        acquireMosUnit = TRANSIT_UNIT_MONTH;

        // Planetary Acquisition
        usePlanetaryAcquisition = false;
        maxJumpsPlanetaryAcquisition = 2;
        planetAcquisitionFactionLimit = PlanetaryAcquisitionFactionLimit.NEUTRAL;
        planetAcquisitionNoClanCrossover = true;
        noClanPartsFromIS = true;
        penaltyClanPartsFromIS = 4;
        planetAcquisitionVerbose = false;
        // Planet Socio-Industrial Modifiers
        planetTechAcquisitionBonus = new int[6];
        planetTechAcquisitionBonus[EquipmentType.RATING_A] = -1;
        planetTechAcquisitionBonus[EquipmentType.RATING_B] = 0;
        planetTechAcquisitionBonus[EquipmentType.RATING_C] = 1;
        planetTechAcquisitionBonus[EquipmentType.RATING_D] = 2;
        planetTechAcquisitionBonus[EquipmentType.RATING_E] = 4;
        planetTechAcquisitionBonus[EquipmentType.RATING_F] = 8;
        planetIndustryAcquisitionBonus = new int[6];
        planetIndustryAcquisitionBonus[EquipmentType.RATING_A] = 0;
        planetIndustryAcquisitionBonus[EquipmentType.RATING_B] = 0;
        planetIndustryAcquisitionBonus[EquipmentType.RATING_C] = 0;
        planetIndustryAcquisitionBonus[EquipmentType.RATING_D] = 0;
        planetIndustryAcquisitionBonus[EquipmentType.RATING_E] = 0;
        planetIndustryAcquisitionBonus[EquipmentType.RATING_F] = 0;
        planetOutputAcquisitionBonus = new int[6];
        planetOutputAcquisitionBonus[EquipmentType.RATING_A] = -1;
        planetOutputAcquisitionBonus[EquipmentType.RATING_B] = 0;
        planetOutputAcquisitionBonus[EquipmentType.RATING_C] = 1;
        planetOutputAcquisitionBonus[EquipmentType.RATING_D] = 2;
        planetOutputAcquisitionBonus[EquipmentType.RATING_E] = 4;
        planetOutputAcquisitionBonus[EquipmentType.RATING_F] = 8;
        //endregion Supplies and Acquisitions Tab

        //region Tech Limits Tab
        limitByYear = true;
        disallowExtinctStuff = false;
        allowClanPurchases = true;
        allowISPurchases = true;
        allowCanonOnly = false;
        allowCanonRefitOnly = false;
        techLevel = TECH_EXPERIMENTAL;
        variableTechLevel = false;
        factionIntroDate = false;
        useAmmoByType = false;
        //endregion Tech Limits Tab

        //region Personnel Tab
        // General Personnel
        setUseTactics(false);
        setUseInitiativeBonus(false);
        setUseToughness(false);
        setUseArtillery(false);
        setUseAbilities(false);
        setUseEdge(false);
        setUseSupportEdge(false);
        setUseImplants(false);
        setAlternativeQualityAveraging(false);
        setUseTransfers(true);
        setUseExtendedTOEForceName(false);
        setPersonnelLogSkillGain(false);
        setPersonnelLogAbilityGain(false);
        setPersonnelLogEdgeGain(false);

        // Expanded Personnel Information
        setUseTimeInService(false);
        setTimeInServiceDisplayFormat(TimeInDisplayFormat.YEARS);
        setUseTimeInRank(false);
        setTimeInRankDisplayFormat(TimeInDisplayFormat.MONTHS_YEARS);
        setTrackTotalEarnings(false);
        setTrackTotalXPEarnings(false);
        setShowOriginFaction(true);

        // Medical
        setUseAdvancedMedical(false);
        setHealingWaitingPeriod(1);
        setNaturalHealingWaitingPeriod(15);
        setMinimumHitsForVehicles(1);
        setUseRandomHitsForVehicles(false);
        setTougherHealing(false);

        // Prisoners
        setPrisonerCaptureStyle(PrisonerCaptureStyle.TAHARQA);
        setDefaultPrisonerStatus(PrisonerStatus.PRISONER);
        setPrisonerBabyStatus(true);
        setUseAtBPrisonerDefection(false);
        setUseAtBPrisonerRansom(false);

        // Personnel Randomization
        setUseDylansRandomXP(false);
        setRandomOriginOptions(new RandomOriginOptions(true));

        // Retirement
        setUseRetirementDateTracking(false);
        setRandomRetirementMethod(RandomRetirementMethod.NONE);
        setUseYearEndRandomRetirement(true);
        setUseContractCompletionRandomRetirement(true);
        setUseCustomRetirementModifiers(true);
        setUseRandomFounderRetirement(true);
        setTrackUnitFatigue(false);

        // Family
        setDisplayFamilyLevel(FamilialRelationshipDisplayLevel.SPOUSE);

        // Dependent
        setRandomDependentMethod(RandomDependentMethod.NONE);
        setUseRandomDependentAddition(true);
        setUseRandomDependentRemoval(true);

        // Salary
        setSalaryCommissionMultiplier(1.2);
        setSalaryEnlistedMultiplier(1.0);
        setSalaryAntiMekMultiplier(1.5);
        setSalarySpecialistInfantryMultiplier(1.0);
        setSalaryXPMultipliers(new double[5]);
        setSalaryXPMultiplier(SkillType.EXP_ULTRA_GREEN, 0.6);
        setSalaryXPMultiplier(SkillType.EXP_GREEN, 0.6);
        setSalaryXPMultiplier(SkillType.EXP_REGULAR, 1.0);
        setSalaryXPMultiplier(SkillType.EXP_VETERAN, 1.6);
        setSalaryXPMultiplier(SkillType.EXP_ELITE, 3.2);
        setRoleBaseSalaries(new Money[personnelRoles.length]);
        setRoleBaseSalary(PersonnelRole.MECHWARRIOR, 1500);
        setRoleBaseSalary(PersonnelRole.LAM_PILOT, 3000);
        setRoleBaseSalary(PersonnelRole.GROUND_VEHICLE_DRIVER, 900);
        setRoleBaseSalary(PersonnelRole.NAVAL_VEHICLE_DRIVER, 900);
        setRoleBaseSalary(PersonnelRole.VTOL_PILOT, 900);
        setRoleBaseSalary(PersonnelRole.VEHICLE_GUNNER, 900);
        setRoleBaseSalary(PersonnelRole.VEHICLE_CREW, 900);
        setRoleBaseSalary(PersonnelRole.AEROSPACE_PILOT, 1500);
        setRoleBaseSalary(PersonnelRole.CONVENTIONAL_AIRCRAFT_PILOT, 900);
        setRoleBaseSalary(PersonnelRole.PROTOMECH_PILOT, 960);
        setRoleBaseSalary(PersonnelRole.BATTLE_ARMOUR, 960);
        setRoleBaseSalary(PersonnelRole.SOLDIER, 750);
        setRoleBaseSalary(PersonnelRole.VESSEL_PILOT, 1000);
        setRoleBaseSalary(PersonnelRole.VESSEL_GUNNER, 1000);
        setRoleBaseSalary(PersonnelRole.VESSEL_CREW, 1000);
        setRoleBaseSalary(PersonnelRole.VESSEL_NAVIGATOR, 1000);
        setRoleBaseSalary(PersonnelRole.MECH_TECH, 800);
        setRoleBaseSalary(PersonnelRole.MECHANIC, 800);
        setRoleBaseSalary(PersonnelRole.AERO_TECH, 800);
        setRoleBaseSalary(PersonnelRole.BA_TECH, 800);
        setRoleBaseSalary(PersonnelRole.ASTECH, 400);
        setRoleBaseSalary(PersonnelRole.DOCTOR, 1500);
        setRoleBaseSalary(PersonnelRole.MEDIC, 400);
        setRoleBaseSalary(PersonnelRole.ADMINISTRATOR_COMMAND, 500);
        setRoleBaseSalary(PersonnelRole.ADMINISTRATOR_LOGISTICS, 500);
        setRoleBaseSalary(PersonnelRole.ADMINISTRATOR_TRANSPORT, 500);
        setRoleBaseSalary(PersonnelRole.ADMINISTRATOR_HR, 500);
        setRoleBaseSalary(PersonnelRole.DEPENDENT, 0);
        setRoleBaseSalary(PersonnelRole.NONE, 0);

        // Marriage
        setUseManualMarriages(true);
        setUseClannerMarriages(false);
        setUsePrisonerMarriages(true);
        setMinimumMarriageAge(16);
        setCheckMutualAncestorsDepth(4);
        setLogMarriageNameChanges(false);
        setMarriageSurnameWeights(new HashMap<>());
        getMarriageSurnameWeights().put(MergingSurnameStyle.NO_CHANGE, 100);
        getMarriageSurnameWeights().put(MergingSurnameStyle.YOURS, 55);
        getMarriageSurnameWeights().put(MergingSurnameStyle.SPOUSE, 55);
        getMarriageSurnameWeights().put(MergingSurnameStyle.SPACE_YOURS, 10);
        getMarriageSurnameWeights().put(MergingSurnameStyle.BOTH_SPACE_YOURS, 5);
        getMarriageSurnameWeights().put(MergingSurnameStyle.HYPHEN_YOURS, 30);
        getMarriageSurnameWeights().put(MergingSurnameStyle.BOTH_HYPHEN_YOURS, 20);
        getMarriageSurnameWeights().put(MergingSurnameStyle.SPACE_SPOUSE, 10);
        getMarriageSurnameWeights().put(MergingSurnameStyle.BOTH_SPACE_SPOUSE, 5);
        getMarriageSurnameWeights().put(MergingSurnameStyle.HYPHEN_SPOUSE, 30);
        getMarriageSurnameWeights().put(MergingSurnameStyle.BOTH_HYPHEN_SPOUSE, 20);
        getMarriageSurnameWeights().put(MergingSurnameStyle.MALE, 500);
        getMarriageSurnameWeights().put(MergingSurnameStyle.FEMALE, 160);
        setRandomMarriageMethod(RandomMarriageMethod.NONE);
        setUseRandomSameSexMarriages(false);
        setUseRandomClannerMarriages(false);
        setUseRandomPrisonerMarriages(true);
        setRandomMarriageAgeRange(10);
        setPercentageRandomMarriageOppositeSexChance(0.00025);
        setPercentageRandomMarriageSameSexChance(0.00002);

        // Divorce
        setUseManualDivorce(true);
        setUseClannerDivorce(true);
        setUsePrisonerDivorce(false);
        setDivorceSurnameWeights(new HashMap<>());
        getDivorceSurnameWeights().put(SplittingSurnameStyle.ORIGIN_CHANGES_SURNAME, 10);
        getDivorceSurnameWeights().put(SplittingSurnameStyle.SPOUSE_CHANGES_SURNAME, 10);
        getDivorceSurnameWeights().put(SplittingSurnameStyle.BOTH_CHANGE_SURNAME, 30);
        getDivorceSurnameWeights().put(SplittingSurnameStyle.BOTH_KEEP_SURNAME, 50);
        setRandomDivorceMethod(RandomDivorceMethod.NONE);
        setUseRandomOppositeSexDivorce(true);
        setUseRandomSameSexDivorce(true);
        setUseRandomClannerDivorce(true);
        setUseRandomPrisonerDivorce(false);
        setPercentageRandomDivorceOppositeSexChance(0.000001);
        setPercentageRandomDivorceSameSexChance(0.000001);

        // Divorce

        // Procreation
        setUseManualProcreation(true);
        setUseClannerProcreation(false);
        setUsePrisonerProcreation(true);
        setMultiplePregnancyOccurrences(50); // Hellin's Law is 89, but we make it more common so it shows up more
        setBabySurnameStyle(BabySurnameStyle.MOTHERS);
        setAssignNonPrisonerBabiesFounderTag(false);
        setAssignChildrenOfFoundersFounderTag(false);
        setDetermineFatherAtBirth(false);
        setDisplayTrueDueDate(false);
        setLogProcreation(false);
        setRandomProcreationMethod(RandomProcreationMethod.NONE);
        setUseRelationshiplessRandomProcreation(false);
        setUseRandomClannerProcreation(false);
        setUseRandomPrisonerProcreation(true);
        setPercentageRandomProcreationRelationshipChance(0.0005);
        setPercentageRandomProcreationRelationshiplessChance(0.00005);

        // Death
        setKeepMarriedNameUponSpouseDeath(true);
        setRandomDeathMethod(RandomDeathMethod.NONE);
        setEnabledRandomDeathAgeGroups(new HashMap<>());
        getEnabledRandomDeathAgeGroups().put(AgeGroup.ELDER, true);
        getEnabledRandomDeathAgeGroups().put(AgeGroup.ADULT, true);
        getEnabledRandomDeathAgeGroups().put(AgeGroup.TEENAGER, true);
        getEnabledRandomDeathAgeGroups().put(AgeGroup.PRETEEN, false);
        getEnabledRandomDeathAgeGroups().put(AgeGroup.CHILD, false);
        getEnabledRandomDeathAgeGroups().put(AgeGroup.TODDLER, false);
        getEnabledRandomDeathAgeGroups().put(AgeGroup.BABY, false);
        setUseRandomDeathSuicideCause(false);
        setUseRandomClanPersonnelDeath(true);
        setUseRandomPrisonerDeath(true);
        setPercentageRandomDeathChance(0.00002);
        // The following four setups are all based on the 2018 US death rate: https://www.statista.com/statistics/241572/death-rate-by-age-and-sex-in-the-us/
        setExponentialRandomDeathMaleValues(5.4757, -7.0, 0.0709); // base equation of 2 * 10^-4 * e^(0.0709 * age) per year, divided by 365.25
        setExponentialRandomDeathFemaleValues(2.4641, -7.0, 0.0752); // base equation of 9 * 10^-5 * e^(0.0752 * age) per year, divided by 365.25
        setAgeRangeRandomDeathMaleValues(new HashMap<>());
        getAgeRangeRandomDeathMaleValues().put(TenYearAgeRange.UNDER_ONE, 613.1);
        getAgeRangeRandomDeathMaleValues().put(TenYearAgeRange.ONE_FOUR, 27.5);
        getAgeRangeRandomDeathMaleValues().put(TenYearAgeRange.FIVE_FOURTEEN, 14.7);
        getAgeRangeRandomDeathMaleValues().put(TenYearAgeRange.FIFTEEN_TWENTY_FOUR, 100.1);
        getAgeRangeRandomDeathMaleValues().put(TenYearAgeRange.TWENTY_FIVE_THIRTY_FOUR, 176.1);
        getAgeRangeRandomDeathMaleValues().put(TenYearAgeRange.THIRTY_FIVE_FORTY_FOUR, 249.5);
        getAgeRangeRandomDeathMaleValues().put(TenYearAgeRange.FORTY_FIVE_FIFTY_FOUR, 491.8);
        getAgeRangeRandomDeathMaleValues().put(TenYearAgeRange.FIFTY_FIVE_SIXTY_FOUR, 1119.0);
        getAgeRangeRandomDeathMaleValues().put(TenYearAgeRange.SIXTY_FIVE_SEVENTY_FOUR, 2196.5);
        getAgeRangeRandomDeathMaleValues().put(TenYearAgeRange.SEVENTY_FIVE_EIGHTY_FOUR, 5155.0);
        getAgeRangeRandomDeathMaleValues().put(TenYearAgeRange.EIGHTY_FIVE_OR_OLDER, 14504.0);
        setAgeRangeRandomDeathFemaleValues(new HashMap<>());
        getAgeRangeRandomDeathFemaleValues().put(TenYearAgeRange.UNDER_ONE, 500.0);
        getAgeRangeRandomDeathFemaleValues().put(TenYearAgeRange.ONE_FOUR, 20.4);
        getAgeRangeRandomDeathFemaleValues().put(TenYearAgeRange.FIVE_FOURTEEN, 11.8);
        getAgeRangeRandomDeathFemaleValues().put(TenYearAgeRange.FIFTEEN_TWENTY_FOUR, 38.8);
        getAgeRangeRandomDeathFemaleValues().put(TenYearAgeRange.TWENTY_FIVE_THIRTY_FOUR, 80.0);
        getAgeRangeRandomDeathFemaleValues().put(TenYearAgeRange.THIRTY_FIVE_FORTY_FOUR, 140.2);
        getAgeRangeRandomDeathFemaleValues().put(TenYearAgeRange.FORTY_FIVE_FIFTY_FOUR, 302.5);
        getAgeRangeRandomDeathFemaleValues().put(TenYearAgeRange.FIFTY_FIVE_SIXTY_FOUR, 670.0);
        getAgeRangeRandomDeathFemaleValues().put(TenYearAgeRange.SIXTY_FIVE_SEVENTY_FOUR, 1421.0);
        getAgeRangeRandomDeathFemaleValues().put(TenYearAgeRange.SEVENTY_FIVE_EIGHTY_FOUR, 3788.0);
        getAgeRangeRandomDeathFemaleValues().put(TenYearAgeRange.EIGHTY_FIVE_OR_OLDER, 12870.0);
        //endregion Personnel Tab

        //region Finances Tab
        payForParts = false;
        payForRepairs = false;
        payForUnits = false;
        payForSalaries = false;
        payForOverhead = false;
        payForMaintain = false;
        payForTransport = false;
        sellUnits = false;
        sellParts = false;
        payForRecruitment = false;
        useLoanLimits = false;
        usePercentageMaint = false;
        infantryDontCount = false;
        usePeacetimeCost = false;
        useExtendedPartsModifier = false;
        showPeacetimeCost = false;
        setFinancialYearDuration(FinancialYearDuration.ANNUAL);
        newFinancialYearFinancesToCSVExport = false;

        // Price Multipliers
        setCommonPartPriceMultiplier(1.0);
        setInnerSphereUnitPriceMultiplier(1.0);
        setInnerSpherePartPriceMultiplier(1.0);
        setClanUnitPriceMultiplier(1.0);
        setClanPartPriceMultiplier(1.0);
        setMixedTechUnitPriceMultiplier(1.0);
        setUsedPartPriceMultipliers(0.1, 0.2, 0.3, 0.5, 0.7, 0.9);
        setDamagedPartsValueMultiplier(0.33);
        setUnrepairablePartsValueMultiplier(0.1);
        setCancelledOrderRefundMultiplier(0.5);
        //endregion Finances Tab

        //region Mercenary Tab
        equipmentContractBase = false;
        equipmentContractPercent = 5.0;
        equipmentContractSaleValue = false;
        dropshipContractPercent = 1.0;
        jumpshipContractPercent = 0.0;
        warshipContractPercent = 0.0;
        blcSaleValue = false;
        overageRepaymentInFinalPayment = false;
        //endregion Mercenary Tab

        //region Experience Tab
        scenarioXP = 1;
        killXPAward = 0;
        killsForXP = 0;
        tasksXP = 1;
        nTasksXP = 25;
        successXP = 0;
        mistakeXP = 0;
        idleXP = 0;
        monthsIdleXP = 2;
        targetIdleXP = 10;
        contractNegotiationXP = 0;
        adminXP = 0;
        adminXPPeriod = 1;
        edgeCost = 10;
        //endregion Experience Tab

        //region Skills Tab
        //endregion Skills Tab

        //region Special Abilities Tab
        //endregion Special Abilities Tab

        //region Skill Randomization Tab
        phenotypeProbabilities = new int[Phenotype.getExternalPhenotypes().size()];
        phenotypeProbabilities[Phenotype.MECHWARRIOR.ordinal()] = 95;
        phenotypeProbabilities[Phenotype.ELEMENTAL.ordinal()] = 100;
        phenotypeProbabilities[Phenotype.AEROSPACE.ordinal()] = 95;
        phenotypeProbabilities[Phenotype.VEHICLE.ordinal()] = 0;
        phenotypeProbabilities[Phenotype.PROTOMECH.ordinal()] = 95;
        phenotypeProbabilities[Phenotype.NAVAL.ordinal()] = 25;
        //endregion Skill Randomization Tab

        //region Rank System Tab
        //endregion Rank System Tab

        //region Name and Portrait Generation Tab
        useOriginFactionForNames = true;
        usePortraitForRole = new boolean[personnelRoles.length];
        Arrays.fill(usePortraitForRole, false);
        usePortraitForRole[PersonnelRole.MECHWARRIOR.ordinal()] = true;
        assignPortraitOnRoleChange = false;
        //endregion Name and Portrait Generation Tab

        //region Markets Tab
        // Personnel Market
        setPersonnelMarketType(PersonnelMarket.getTypeName(PersonnelMarket.TYPE_STRAT_OPS));
        setPersonnelMarketReportRefresh(true);
        setPersonnelMarketRandomEliteRemoval(10);
        setPersonnelMarketRandomVeteranRemoval(8);
        setPersonnelMarketRandomRegularRemoval(6);
        setPersonnelMarketRandomGreenRemoval(4);
        setPersonnelMarketRandomUltraGreenRemoval(4);
        setPersonnelMarketDylansWeight(0.3);

        // Unit Market
        setUnitMarketMethod(UnitMarketMethod.NONE);
        setUnitMarketRegionalMechVariations(true);
        setInstantUnitMarketDelivery(false);
        setUnitMarketReportRefresh(true);

        // Contract Market
        setContractMarketMethod(ContractMarketMethod.NONE);
        setContractMarketReportRefresh(true);
        //endregion Markets Tab

        //region RATs Tab
        setUseStaticRATs(false);
        setRATs("Xotl", "Total Warfare");
        setIgnoreRATEra(false);
        //endregion RATs Tab

        //region Against the Bot Tab
        useAtB = false;
        useStratCon = false;
        skillLevel = 2;

        // Unit Administration
        useShareSystem = false;
        sharesExcludeLargeCraft = false;
        sharesForAll = false;
        aeroRecruitsHaveUnits = false;
        useLeadership = true;
        trackOriginalUnit = false;
        useAero = false;
        useVehicles = true;
        clanVehicles = false;

        // Contract Operations
        searchRadius = 800;
        variableContractLength = false;
        mercSizeLimited = false;
        restrictPartsByMission = true;
        limitLanceWeight = true;
        limitLanceNumUnits = true;
        useStrategy = true;
        baseStrategyDeployment = 3;
        additionalStrategyDeployment = 1;
        adjustPaymentForStrategy = false;
        atbBattleChance = new int[AtBLanceRole.values().length - 1];
        atbBattleChance[AtBLanceRole.FIGHTING.ordinal()] = 40;
        atbBattleChance[AtBLanceRole.DEFENCE.ordinal()] = 20;
        atbBattleChance[AtBLanceRole.SCOUTING.ordinal()] = 60;
        atbBattleChance[AtBLanceRole.TRAINING.ordinal()] = 10;
        generateChases = true;

        // Scenarios
        doubleVehicles = false;
        opforLanceTypeMechs = 1;
        opforLanceTypeMixed = 2;
        opforLanceTypeVehicles = 3;
        opforUsesVTOLs = true;
        allowOpforAeros = false;
        opforAeroChance = 5;
        allowOpforLocalUnits = false;
        opforLocalUnitChance = 5;
        setFixedMapChance(25);
        setSpaUpgradeIntensity(0);
        adjustPlayerVehicles = false;
        regionalMechVariations = false;
        attachedPlayerCamouflage = true;
        playerControlsAttachedUnits = false;
        useDropShips = false;
        useWeatherConditions = true;
        useLightConditions = true;
        usePlanetaryConditions = false;
        //endregion Against the Bot Tab
    }
    //endregion Constructors

    //region General Tab
    /**
     * @return the method of unit rating to use
     */
    public UnitRatingMethod getUnitRatingMethod() {
        return unitRatingMethod;
    }

    /**
     * @param method the method of unit rating to use
     */
    public void setUnitRatingMethod(UnitRatingMethod method) {
        this.unitRatingMethod = method;
    }

    public int getManualUnitRatingModifier() {
        return manualUnitRatingModifier;
    }

    public void setManualUnitRatingModifier(int manualUnitRatingModifier) {
        this.manualUnitRatingModifier = manualUnitRatingModifier;
    }
    //endregion General Tab

    //region Repair and Maintenance Tab
    //region Repair
    //endregion Repair

    //region Maintenance
    public boolean checkMaintenance() {
        return checkMaintenance;
    }

    public void setCheckMaintenance(boolean b) {
        checkMaintenance = b;
    }

    public int getMaintenanceCycleDays() {
        return maintenanceCycleDays;
    }

    public void setMaintenanceCycleDays(int d) {
        maintenanceCycleDays = d;
    }

    public int getMaintenanceBonus() {
        return maintenanceBonus;
    }

    public void setMaintenanceBonus(int d) {
        maintenanceBonus = d;
    }

    public boolean useQualityMaintenance() {
        return useQualityMaintenance;
    }

    public void setUseQualityMaintenance(boolean b) {
        useQualityMaintenance = b;
    }

    public boolean reverseQualityNames() {
        return reverseQualityNames;
    }

    public void setReverseQualityNames(boolean b) {
        reverseQualityNames = b;
    }

    public boolean useUnofficialMaintenance() {
        return useUnofficialMaintenance;
    }

    public void setUseUnofficialMaintenance(boolean b) {
        useUnofficialMaintenance = b;
    }

    public boolean logMaintenance() {
        return logMaintenance;
    }

    public void setLogMaintenance(boolean b) {
        logMaintenance = b;
    }
    //endregion Maintenance
    //endregion Repair and Maintenance Tab

    //region Supplies and Acquisitions Tab
    //endregion Supplies and Acquisitions Tab

    //region Personnel Tab
    //region General Personnel
    public boolean useTactics() {
        return useTactics;
    }

    public void setUseTactics(final boolean useTactics) {
        this.useTactics = useTactics;
    }

    public boolean useInitiativeBonus() {
        return useInitiativeBonus;
    }

    public void setUseInitiativeBonus(final boolean useInitiativeBonus) {
        this.useInitiativeBonus = useInitiativeBonus;
    }

    public boolean useToughness() {
        return useToughness;
    }

    public void setUseToughness(final boolean useToughness) {
        this.useToughness = useToughness;
    }

    public boolean useArtillery() {
        return useArtillery;
    }

    public void setUseArtillery(final boolean useArtillery) {
        this.useArtillery = useArtillery;
    }

    public boolean useAbilities() {
        return useAbilities;
    }

    public void setUseAbilities(final boolean useAbilities) {
        this.useAbilities = useAbilities;
    }

    public boolean useEdge() {
        return useEdge;
    }

    public void setUseEdge(final boolean useEdge) {
        this.useEdge = useEdge;
    }

    public boolean useSupportEdge() {
        return useSupportEdge;
    }

    public void setUseSupportEdge(final boolean useSupportEdge) {
        this.useSupportEdge = useSupportEdge;
    }

    public boolean useImplants() {
        return useImplants;
    }

    public void setUseImplants(final boolean useImplants) {
        this.useImplants = useImplants;
    }

    public boolean useAlternativeQualityAveraging() {
        return alternativeQualityAveraging;
    }

    public void setAlternativeQualityAveraging(final boolean alternativeQualityAveraging) {
        this.alternativeQualityAveraging = alternativeQualityAveraging;
    }

    public boolean useTransfers() {
        return useTransfers;
    }

    public void setUseTransfers(final boolean useTransfers) {
        this.useTransfers = useTransfers;
    }

    public boolean isUseExtendedTOEForceName() {
        return useExtendedTOEForceName;
    }

    public void setUseExtendedTOEForceName(final boolean useExtendedTOEForceName) {
        this.useExtendedTOEForceName = useExtendedTOEForceName;
    }

    public boolean isPersonnelLogSkillGain() {
        return personnelLogSkillGain;
    }

    public void setPersonnelLogSkillGain(final boolean personnelLogSkillGain) {
        this.personnelLogSkillGain = personnelLogSkillGain;
    }

    public boolean isPersonnelLogAbilityGain() {
        return personnelLogAbilityGain;
    }

    public void setPersonnelLogAbilityGain(final boolean personnelLogAbilityGain) {
        this.personnelLogAbilityGain = personnelLogAbilityGain;
    }

    public boolean isPersonnelLogEdgeGain() {
        return personnelLogEdgeGain;
    }

    public void setPersonnelLogEdgeGain(final boolean personnelLogEdgeGain) {
        this.personnelLogEdgeGain = personnelLogEdgeGain;
    }
    //endregion General Personnel

    //region Expanded Personnel Information
    /**
     * @return whether or not to use time in service
     */
    public boolean getUseTimeInService() {
        return useTimeInService;
    }

    /**
     * @param useTimeInService the new value for whether to use time in service or not
     */
    public void setUseTimeInService(final boolean useTimeInService) {
        this.useTimeInService = useTimeInService;
    }

    /**
     * @return the format to display the Time in Service in
     */
    public TimeInDisplayFormat getTimeInServiceDisplayFormat() {
        return timeInServiceDisplayFormat;
    }

    /**
     * @param timeInServiceDisplayFormat the new display format for Time in Service
     */
    public void setTimeInServiceDisplayFormat(final TimeInDisplayFormat timeInServiceDisplayFormat) {
        this.timeInServiceDisplayFormat = timeInServiceDisplayFormat;
    }

    /**
     * @return whether or not to use time in rank
     */
    public boolean getUseTimeInRank() {
        return useTimeInRank;
    }

    /**
     * @param useTimeInRank the new value for whether or not to use time in rank
     */
    public void setUseTimeInRank(final boolean useTimeInRank) {
        this.useTimeInRank = useTimeInRank;
    }

    /**
     * @return the format to display the Time in Rank in
     */
    public TimeInDisplayFormat getTimeInRankDisplayFormat() {
        return timeInRankDisplayFormat;
    }

    /**
     * @param timeInRankDisplayFormat the new display format for Time in Rank
     */
    public void setTimeInRankDisplayFormat(final TimeInDisplayFormat timeInRankDisplayFormat) {
        this.timeInRankDisplayFormat = timeInRankDisplayFormat;
    }

    /**
     * @return whether or not to track the total earnings of personnel
     */
    public boolean isTrackTotalEarnings() {
        return trackTotalEarnings;
    }

    /**
     * @param trackTotalEarnings the new value for whether or not to track total earnings for personnel
     */
    public void setTrackTotalEarnings(final boolean trackTotalEarnings) {
        this.trackTotalEarnings = trackTotalEarnings;
    }

    /**
     * @return whether or not to track the total experience earnings of personnel
     */
    public boolean isTrackTotalXPEarnings() {
        return trackTotalXPEarnings;
    }

    /**
     * @param trackTotalXPEarnings the new value for whether or not to track total experience
     *                             earnings for personnel
     */
    public void setTrackTotalXPEarnings(final boolean trackTotalXPEarnings) {
        this.trackTotalXPEarnings = trackTotalXPEarnings;
    }

    /**
     * Gets a value indicating whether or not to show a person's origin faction when displaying
     * their details.
     */
    public boolean showOriginFaction() {
        return showOriginFaction;
    }

    /**
     * Sets a value indicating whether or not to show a person's origin faction when displaying
     * their details.
     */
    public void setShowOriginFaction(final boolean showOriginFaction) {
        this.showOriginFaction = showOriginFaction;
    }
    //endregion Expanded Personnel Information

    //region Medical
    public boolean useAdvancedMedical() {
        return useAdvancedMedical;
    }

    public void setUseAdvancedMedical(final boolean useAdvancedMedical) {
        this.useAdvancedMedical = useAdvancedMedical;
    }

    public int getHealingWaitingPeriod() {
        return healWaitingPeriod;
    }

    public void setHealingWaitingPeriod(final int healWaitingPeriod) {
        this.healWaitingPeriod = healWaitingPeriod;
    }

    public int getNaturalHealingWaitingPeriod() {
        return naturalHealingWaitingPeriod;
    }

    public void setNaturalHealingWaitingPeriod(final int naturalHealingWaitingPeriod) {
        this.naturalHealingWaitingPeriod = naturalHealingWaitingPeriod;
    }

    public int getMinimumHitsForVehicles() {
        return minimumHitsForVehicles;
    }

    public void setMinimumHitsForVehicles(final int minimumHitsForVehicles) {
        this.minimumHitsForVehicles = minimumHitsForVehicles;
    }

    public boolean useRandomHitsForVehicles() {
        return useRandomHitsForVehicles;
    }

    public void setUseRandomHitsForVehicles(final boolean useRandomHitsForVehicles) {
        this.useRandomHitsForVehicles = useRandomHitsForVehicles;
    }

    public boolean useTougherHealing() {
        return tougherHealing;
    }

    public void setTougherHealing(final boolean tougherHealing) {
        this.tougherHealing = tougherHealing;
    }
    //endregion Medical

    //region Prisoners
    public PrisonerCaptureStyle getPrisonerCaptureStyle() {
        return prisonerCaptureStyle;
    }

    public void setPrisonerCaptureStyle(final PrisonerCaptureStyle prisonerCaptureStyle) {
        this.prisonerCaptureStyle = prisonerCaptureStyle;
    }

    public PrisonerStatus getDefaultPrisonerStatus() {
        return defaultPrisonerStatus;
    }

    public void setDefaultPrisonerStatus(final PrisonerStatus defaultPrisonerStatus) {
        this.defaultPrisonerStatus = defaultPrisonerStatus;
    }

    public boolean getPrisonerBabyStatus() {
        return prisonerBabyStatus;
    }

    public void setPrisonerBabyStatus(final boolean prisonerBabyStatus) {
        this.prisonerBabyStatus = prisonerBabyStatus;
    }

    public boolean useAtBPrisonerDefection() {
        return useAtBPrisonerDefection;
    }

    public void setUseAtBPrisonerDefection(final boolean useAtBPrisonerDefection) {
        this.useAtBPrisonerDefection = useAtBPrisonerDefection;
    }

    public boolean useAtBPrisonerRansom() {
        return useAtBPrisonerRansom;
    }

    public void setUseAtBPrisonerRansom(final boolean useAtBPrisonerRansom) {
        this.useAtBPrisonerRansom = useAtBPrisonerRansom;
    }
    //endregion Prisoners

    //region Personnel Randomization
    public boolean useDylansRandomXP() {
        return useDylansRandomXP;
    }

    public void setUseDylansRandomXP(final boolean useDylansRandomXP) {
        this.useDylansRandomXP = useDylansRandomXP;
    }

    public RandomOriginOptions getRandomOriginOptions() {
        return randomOriginOptions;
    }

    public void setRandomOriginOptions(final RandomOriginOptions randomOriginOptions) {
        this.randomOriginOptions = randomOriginOptions;
    }
    //endregion Personnel Randomization

    //region Retirement
    /**
     * @return whether to track retirement dates
     */
    public boolean isUseRetirementDateTracking() {
        return useRetirementDateTracking;
    }

    /**
     * @param useRetirementDateTracking the new value for whether to track retirement dates
     */
    public void setUseRetirementDateTracking(final boolean useRetirementDateTracking) {
        this.useRetirementDateTracking = useRetirementDateTracking;
    }

    public RandomRetirementMethod getRandomRetirementMethod() {
        return randomRetirementMethod;
    }

    public void setRandomRetirementMethod(final RandomRetirementMethod randomRetirementMethod) {
        this.randomRetirementMethod = randomRetirementMethod;
    }

    public boolean isUseYearEndRandomRetirement() {
        return useYearEndRandomRetirement;
    }

    public void setUseYearEndRandomRetirement(final boolean useYearEndRandomRetirement) {
        this.useYearEndRandomRetirement = useYearEndRandomRetirement;
    }

    public boolean isUseContractCompletionRandomRetirement() {
        return useContractCompletionRandomRetirement;
    }

    public void setUseContractCompletionRandomRetirement(final boolean useContractCompletionRandomRetirement) {
        this.useContractCompletionRandomRetirement = useContractCompletionRandomRetirement;
    }

    public boolean isUseCustomRetirementModifiers() {
        return useCustomRetirementModifiers;
    }

    public void setUseCustomRetirementModifiers(final boolean useCustomRetirementModifiers) {
        this.useCustomRetirementModifiers = useCustomRetirementModifiers;
    }

    public boolean isUseRandomFounderRetirement() {
        return useRandomFounderRetirement;
    }

    public void setUseRandomFounderRetirement(final boolean useRandomFounderRetirement) {
        this.useRandomFounderRetirement = useRandomFounderRetirement;
    }

    public boolean isTrackUnitFatigue() {
        return trackUnitFatigue;
    }

    public void setTrackUnitFatigue(final boolean trackUnitFatigue) {
        this.trackUnitFatigue = trackUnitFatigue;
    }
    //endregion Retirement

    //region Family
    /**
     * @return the level of familial relation to display
     */
    public FamilialRelationshipDisplayLevel getDisplayFamilyLevel() {
        return displayFamilyLevel;
    }

    /**
     * @param displayFamilyLevel the level of familial relation to display
     */
    public void setDisplayFamilyLevel(final FamilialRelationshipDisplayLevel displayFamilyLevel) {
        this.displayFamilyLevel = displayFamilyLevel;
    }
    //endregion Family

    //region Dependent
    public RandomDependentMethod getRandomDependentMethod() {
        return randomDependentMethod;
    }

    public void setRandomDependentMethod(final RandomDependentMethod randomDependentMethod) {
        this.randomDependentMethod = randomDependentMethod;
    }

    public boolean isUseRandomDependentAddition() {
        return useRandomDependentAddition;
    }

    public void setUseRandomDependentAddition(final boolean useRandomDependentAddition) {
        this.useRandomDependentAddition = useRandomDependentAddition;
    }

    public boolean isUseRandomDependentsRemoval() {
        return useRandomDependentRemoval;
    }

    public void setUseRandomDependentRemoval(final boolean useRandomDependentRemoval) {
        this.useRandomDependentRemoval = useRandomDependentRemoval;
    }
    //endregion Dependent

    //region Salary
    public double getSalaryCommissionMultiplier() {
        return salaryCommissionMultiplier;
    }

    public void setSalaryCommissionMultiplier(final double salaryCommissionMultiplier) {
        this.salaryCommissionMultiplier = salaryCommissionMultiplier;
    }

    public double getSalaryEnlistedMultiplier() {
        return salaryEnlistedMultiplier;
    }

    public void setSalaryEnlistedMultiplier(final double salaryEnlistedMultiplier) {
        this.salaryEnlistedMultiplier = salaryEnlistedMultiplier;
    }

    public double getSalaryAntiMekMultiplier() {
        return salaryAntiMekMultiplier;
    }

    public void setSalaryAntiMekMultiplier(final double salaryAntiMekMultiplier) {
        this.salaryAntiMekMultiplier = salaryAntiMekMultiplier;
    }

    public double getSalarySpecialistInfantryMultiplier() {
        return salarySpecialistInfantryMultiplier;
    }

    public void setSalarySpecialistInfantryMultiplier(final double salarySpecialistInfantryMultiplier) {
        this.salarySpecialistInfantryMultiplier = salarySpecialistInfantryMultiplier;
    }

    public double[] getSalaryXPMultipliers() {
        return salaryXPMultipliers;
    }

    public double getSalaryXPMultiplier(final int index) {
        return ((index < 0) || (index >= getSalaryXPMultipliers().length)) ? 1.0 : getSalaryXPMultipliers()[index];
    }

    public void setSalaryXPMultipliers(final double... salaryXPMultipliers) {
        this.salaryXPMultipliers = salaryXPMultipliers;
    }

    public void setSalaryXPMultiplier(final int index, final double multiplier) {
        if ((index < 0) || (index >= getSalaryXPMultipliers().length)) {
            return;
        }
        getSalaryXPMultipliers()[index] = multiplier;
    }

    public Money[] getRoleBaseSalaries() {
        return roleBaseSalaries;
    }

    public void setRoleBaseSalaries(final Money... roleBaseSalaries) {
        this.roleBaseSalaries = roleBaseSalaries;
    }

    public void setRoleBaseSalary(final PersonnelRole role, final double base) {
        setRoleBaseSalary(role, Money.of(base));
    }

    public void setRoleBaseSalary(final PersonnelRole role, final Money base) {
        getRoleBaseSalaries()[role.ordinal()] = base;
    }
    //endregion Salary

    //region Marriage
    /**
     * @return whether or not to use manual marriages
     */
    public boolean isUseManualMarriages() {
        return useManualMarriages;
    }

    /**
     * @param useManualMarriages whether or not to use manual marriages
     */
    public void setUseManualMarriages(final boolean useManualMarriages) {
        this.useManualMarriages = useManualMarriages;
    }

    public boolean isUseClannerMarriages() {
        return useClannerMarriages;
    }

    public void setUseClannerMarriages(final boolean useClannerMarriages) {
        this.useClannerMarriages = useClannerMarriages;
    }

    public boolean isUsePrisonerMarriages() {
        return usePrisonerMarriages;
    }

    public void setUsePrisonerMarriages(final boolean usePrisonerMarriages) {
        this.usePrisonerMarriages = usePrisonerMarriages;
    }

    /**
     * @return the minimum age a person can get married at
     */
    public int getMinimumMarriageAge() {
        return minimumMarriageAge;
    }

    /**
     * @param minimumMarriageAge the minimum age a person can get married at
     */
    public void setMinimumMarriageAge(final int minimumMarriageAge) {
        this.minimumMarriageAge = minimumMarriageAge;
    }

    /**
     * This gets the number of recursions to use when checking mutual ancestors between two personnel
     * @return the number of recursions to use
     */
    public int getCheckMutualAncestorsDepth() {
        return checkMutualAncestorsDepth;
    }

    /**
     * This sets the number of recursions to use when checking mutual ancestors between two personnel
     * @param checkMutualAncestorsDepth the number of recursions
     */
    public void setCheckMutualAncestorsDepth(final int checkMutualAncestorsDepth) {
        this.checkMutualAncestorsDepth = checkMutualAncestorsDepth;
    }

    /**
     * @return whether or not to log a name change in a marriage
     */
    public boolean isLogMarriageNameChanges() {
        return logMarriageNameChanges;
    }

    /**
     * @param logMarriageNameChanges whether to log marriage name changes or not
     */
    public void setLogMarriageNameChanges(final boolean logMarriageNameChanges) {
        this.logMarriageNameChanges = logMarriageNameChanges;
    }

    /**
     * @return the weight map of potential surname changes for weighted marriage surname generation
     */
    public Map<MergingSurnameStyle, Integer> getMarriageSurnameWeights() {
        return marriageSurnameWeights;
    }

    /**
     * @param marriageSurnameWeights the new marriage surname weight map
     */
    public void setMarriageSurnameWeights(final Map<MergingSurnameStyle, Integer> marriageSurnameWeights) {
        this.marriageSurnameWeights = marriageSurnameWeights;
    }

    public RandomMarriageMethod getRandomMarriageMethod() {
        return randomMarriageMethod;
    }

    public void setRandomMarriageMethod(final RandomMarriageMethod randomMarriageMethod) {
        this.randomMarriageMethod = randomMarriageMethod;
    }

    /**
     * @return whether or not to use random same sex marriages
     */
    public boolean isUseRandomSameSexMarriages() {
        return useRandomSameSexMarriages;
    }

    /**
     * @param useRandomSameSexMarriages whether or not to use random same sex marriages
     */
    public void setUseRandomSameSexMarriages(final boolean useRandomSameSexMarriages) {
        this.useRandomSameSexMarriages = useRandomSameSexMarriages;
    }

    public boolean isUseRandomClannerMarriages() {
        return useRandomClannerMarriages;
    }

    public void setUseRandomClannerMarriages(final boolean useRandomClannerMarriages) {
        this.useRandomClannerMarriages = useRandomClannerMarriages;
    }

    public boolean isUseRandomPrisonerMarriages() {
        return useRandomPrisonerMarriages;
    }

    public void setUseRandomPrisonerMarriages(final boolean useRandomPrisonerMarriages) {
        this.useRandomPrisonerMarriages = useRandomPrisonerMarriages;
    }

    /**
     * A random marriage can only happen between two people whose ages differ (+/-) by the returned value
     * @return the age range ages can differ (+/-)
     */
    public int getRandomMarriageAgeRange() {
        return randomMarriageAgeRange;
    }

    /**
     * A random marriage can only happen between two people whose ages differ (+/-) by this value
     * @param randomMarriageAgeRange the new maximum age range
     */
    public void setRandomMarriageAgeRange(final int randomMarriageAgeRange) {
        this.randomMarriageAgeRange = randomMarriageAgeRange;
    }

    /**
     * This gets the decimal chance (between 0 and 1) of a random opposite sex marriage occurring
     * @return the chance, with a value between 0 and 1
     */
    public double getPercentageRandomMarriageOppositeSexChance() {
        return percentageRandomMarriageOppositeSexChance;
    }

    /**
     * This sets the decimal chance (between 0 and 1) of a random opposite sex marriage occurring
     * @param percentageRandomMarriageOppositeSexChance the chance, with a value between 0 and 1
     */
    public void setPercentageRandomMarriageOppositeSexChance(final double percentageRandomMarriageOppositeSexChance) {
        this.percentageRandomMarriageOppositeSexChance = percentageRandomMarriageOppositeSexChance;
    }

    /**
     * This gets the decimal chance (between 0 and 1) of a random same sex marriage occurring
     * @return the chance, with a value between 0 and 1
     */
    public double getPercentageRandomMarriageSameSexChance() {
        return percentageRandomMarriageSameSexChance;
    }

    /**
     * This sets the decimal chance (between 0 and 1) of a random same sex marriage occurring
     * @param percentageRandomMarriageSameSexChance the chance, with a value between 0 and 1
     */
    public void setPercentageRandomMarriageSameSexChance(final double percentageRandomMarriageSameSexChance) {
        this.percentageRandomMarriageSameSexChance = percentageRandomMarriageSameSexChance;
    }
    //endregion Marriage

    //region Divorce
    public boolean isUseManualDivorce() {
        return useManualDivorce;
    }

    public void setUseManualDivorce(final boolean useManualDivorce) {
        this.useManualDivorce = useManualDivorce;
    }

    public boolean isUseClannerDivorce() {
        return useClannerDivorce;
    }

    public void setUseClannerDivorce(final boolean useClannerDivorce) {
        this.useClannerDivorce = useClannerDivorce;
    }

    public boolean isUsePrisonerDivorce() {
        return usePrisonerDivorce;
    }

    public void setUsePrisonerDivorce(final boolean usePrisonerDivorce) {
        this.usePrisonerDivorce = usePrisonerDivorce;
    }

    public Map<SplittingSurnameStyle, Integer> getDivorceSurnameWeights() {
        return divorceSurnameWeights;
    }

    public void setDivorceSurnameWeights(final Map<SplittingSurnameStyle, Integer> divorceSurnameWeights) {
        this.divorceSurnameWeights = divorceSurnameWeights;
    }

    public RandomDivorceMethod getRandomDivorceMethod() {
        return randomDivorceMethod;
    }

    public void setRandomDivorceMethod(final RandomDivorceMethod randomDivorceMethod) {
        this.randomDivorceMethod = randomDivorceMethod;
    }

    public boolean isUseRandomOppositeSexDivorce() {
        return useRandomOppositeSexDivorce;
    }

    public void setUseRandomOppositeSexDivorce(final boolean useRandomOppositeSexDivorce) {
        this.useRandomOppositeSexDivorce = useRandomOppositeSexDivorce;
    }

    public boolean isUseRandomSameSexDivorce() {
        return useRandomSameSexDivorce;
    }

    public void setUseRandomSameSexDivorce(final boolean useRandomSameSexDivorce) {
        this.useRandomSameSexDivorce = useRandomSameSexDivorce;
    }

    public boolean isUseRandomClannerDivorce() {
        return useRandomClannerDivorce;
    }

    public void setUseRandomClannerDivorce(final boolean useRandomClannerDivorce) {
        this.useRandomClannerDivorce = useRandomClannerDivorce;
    }

    public boolean isUseRandomPrisonerDivorce() {
        return useRandomPrisonerDivorce;
    }

    public void setUseRandomPrisonerDivorce(final boolean useRandomPrisonerDivorce) {
        this.useRandomPrisonerDivorce = useRandomPrisonerDivorce;
    }

    public double getPercentageRandomDivorceOppositeSexChance() {
        return percentageRandomDivorceOppositeSexChance;
    }

    public void setPercentageRandomDivorceOppositeSexChance(final double percentageRandomDivorceOppositeSexChance) {
        this.percentageRandomDivorceOppositeSexChance = percentageRandomDivorceOppositeSexChance;
    }

    public double getPercentageRandomDivorceSameSexChance() {
        return percentageRandomDivorceSameSexChance;
    }

    public void setPercentageRandomDivorceSameSexChance(final double percentageRandomDivorceSameSexChance) {
        this.percentageRandomDivorceSameSexChance = percentageRandomDivorceSameSexChance;
    }
    //endregion Divorce

    //region Procreation
    public boolean isUseManualProcreation() {
        return useManualProcreation;
    }

    public void setUseManualProcreation(final boolean useManualProcreation) {
        this.useManualProcreation = useManualProcreation;
    }

    public boolean isUseClannerProcreation() {
        return useClannerProcreation;
    }

    public void setUseClannerProcreation(final boolean useClannerProcreation) {
        this.useClannerProcreation = useClannerProcreation;
    }

    public boolean isUsePrisonerProcreation() {
        return usePrisonerProcreation;
    }

    public void setUsePrisonerProcreation(final boolean usePrisonerProcreation) {
        this.usePrisonerProcreation = usePrisonerProcreation;
    }

    /**
     * @return the X occurrences for there to be a single multiple child occurrence (i.e. 1 in X)
     */
    public int getMultiplePregnancyOccurrences() {
        return multiplePregnancyOccurrences;
    }

    /**
     * @param multiplePregnancyOccurrences the number of occurrences for there to be a single
     *                                     occurrence of a multiple child pregnancy (i.e. 1 in X)
     */
    public void setMultiplePregnancyOccurrences(final int multiplePregnancyOccurrences) {
        this.multiplePregnancyOccurrences = multiplePregnancyOccurrences;
    }

    /**
     * @return what style of surname to use for a baby
     */
    public BabySurnameStyle getBabySurnameStyle() {
        return babySurnameStyle;
    }

    /**
     * @param babySurnameStyle the style of surname to use for a baby
     */
    public void setBabySurnameStyle(final BabySurnameStyle babySurnameStyle) {
        this.babySurnameStyle = babySurnameStyle;
    }

    public boolean isAssignNonPrisonerBabiesFounderTag() {
        return assignNonPrisonerBabiesFounderTag;
    }

    public void setAssignNonPrisonerBabiesFounderTag(final boolean assignNonPrisonerBabiesFounderTag) {
        this.assignNonPrisonerBabiesFounderTag = assignNonPrisonerBabiesFounderTag;
    }

    public boolean isAssignChildrenOfFoundersFounderTag() {
        return assignChildrenOfFoundersFounderTag;
    }

    public void setAssignChildrenOfFoundersFounderTag(final boolean assignChildrenOfFoundersFounderTag) {
        this.assignChildrenOfFoundersFounderTag = assignChildrenOfFoundersFounderTag;
    }

    /**
     * @return whether or not to determine the father at birth instead of at conception
     */
    public boolean isDetermineFatherAtBirth() {
        return determineFatherAtBirth;
    }

    /**
     * @param determineFatherAtBirth whether or not to determine the father at birth instead of at conception
     */
    public void setDetermineFatherAtBirth(final boolean determineFatherAtBirth) {
        this.determineFatherAtBirth = determineFatherAtBirth;
    }

    /**
     * @return whether to show the expected or actual due date for personnel
     */
    public boolean isDisplayTrueDueDate() {
        return displayTrueDueDate;
    }

    /**
     * @param displayTrueDueDate whether to show the expected or actual due date for personnel
     */
    public void setDisplayTrueDueDate(final boolean displayTrueDueDate) {
        this.displayTrueDueDate = displayTrueDueDate;
    }

    /**
     * @return whether to log procreation
     */
    public boolean isLogProcreation() {
        return logProcreation;
    }

    /**
     * @param logProcreation whether to log procreation
     */
    public void setLogProcreation(final boolean logProcreation) {
        this.logProcreation = logProcreation;
    }

    public RandomProcreationMethod getRandomProcreationMethod() {
        return randomProcreationMethod;
    }

    public void setRandomProcreationMethod(final RandomProcreationMethod randomProcreationMethod) {
        this.randomProcreationMethod = randomProcreationMethod;
    }

    /**
     * @return whether or not to use random procreation for personnel without a spouse
     */
    public boolean isUseRelationshiplessRandomProcreation() {
        return useRelationshiplessRandomProcreation;
    }

    /**
     * @param useRelationshiplessRandomProcreation whether or not to use random procreation without a spouse
     */
    public void setUseRelationshiplessRandomProcreation(final boolean useRelationshiplessRandomProcreation) {
        this.useRelationshiplessRandomProcreation = useRelationshiplessRandomProcreation;
    }

    public boolean isUseRandomClannerProcreation() {
        return useRandomClannerProcreation;
    }

    public void setUseRandomClannerProcreation(final boolean useRandomClannerProcreation) {
        this.useRandomClannerProcreation = useRandomClannerProcreation;
    }

    public boolean isUseRandomPrisonerProcreation() {
        return useRandomPrisonerProcreation;
    }

    public void setUseRandomPrisonerProcreation(final boolean useRandomPrisonerProcreation) {
        this.useRandomPrisonerProcreation = useRandomPrisonerProcreation;
    }

    /**
     * This gets the decimal chance (between 0 and 1) of random procreation occurring
     * @return the chance, with a value between 0 and 1
     */
    public double getPercentageRandomProcreationRelationshipChance() {
        return percentageRandomProcreationRelationshipChance;
    }

    /**
     * This sets the decimal chance (between 0 and 1) of random procreation occurring
     * @param percentageRandomProcreationRelationshipChance the chance, with a value between 0 and 1
     */
    public void setPercentageRandomProcreationRelationshipChance(final double percentageRandomProcreationRelationshipChance) {
        this.percentageRandomProcreationRelationshipChance = percentageRandomProcreationRelationshipChance;
    }

    /**
     * This gets the decimal chance (between 0 and 1) of random procreation occurring without a relationship
     * @return the chance, with a value between 0 and 1
     */
    public double getPercentageRandomProcreationRelationshiplessChance() {
        return percentageRandomProcreationRelationshiplessChance;
    }

    /**
     * This sets the decimal chance (between 0 and 1) of random procreation occurring without a relationship
     * @param percentageRandomProcreationRelationshiplessChance the chance, with a value between 0 and 1
     */
    public void setPercentageRandomProcreationRelationshiplessChance(final double percentageRandomProcreationRelationshiplessChance) {
        this.percentageRandomProcreationRelationshiplessChance = percentageRandomProcreationRelationshiplessChance;
    }
    //endregion Procreation

    //region Death
    /**
     * @return whether to keep ones married name upon spouse death or not
     */
    public boolean getKeepMarriedNameUponSpouseDeath() {
        return keepMarriedNameUponSpouseDeath;
    }

    /**
     * @param keepMarriedNameUponSpouseDeath whether to keep ones married name upon spouse death or not
     */
    public void setKeepMarriedNameUponSpouseDeath(final boolean keepMarriedNameUponSpouseDeath) {
        this.keepMarriedNameUponSpouseDeath = keepMarriedNameUponSpouseDeath;
    }

    /**
     * @return the random death method to use
     */
    public RandomDeathMethod getRandomDeathMethod() {
        return randomDeathMethod;
    }

    /**
     * @param randomDeathMethod the random death method to use
     */
    public void setRandomDeathMethod(final RandomDeathMethod randomDeathMethod) {
        this.randomDeathMethod = randomDeathMethod;
    }

    public Map<AgeGroup, Boolean> getEnabledRandomDeathAgeGroups() {
        return enabledRandomDeathAgeGroups;
    }

    public void setEnabledRandomDeathAgeGroups(final Map<AgeGroup, Boolean> enabledRandomDeathAgeGroups) {
        this.enabledRandomDeathAgeGroups = enabledRandomDeathAgeGroups;
    }

    public boolean isUseRandomClanPersonnelDeath() {
        return useRandomClanPersonnelDeath;
    }

    public void setUseRandomClanPersonnelDeath(final boolean useRandomClanPersonnelDeath) {
        this.useRandomClanPersonnelDeath = useRandomClanPersonnelDeath;
    }

    public boolean isUseRandomPrisonerDeath() {
        return useRandomPrisonerDeath;
    }

    public void setUseRandomPrisonerDeath(final boolean useRandomPrisonerDeath) {
        this.useRandomPrisonerDeath = useRandomPrisonerDeath;
    }

    public boolean isUseRandomDeathSuicideCause() {
        return useRandomDeathSuicideCause;
    }

    public void setUseRandomDeathSuicideCause(final boolean useRandomDeathSuicideCause) {
        this.useRandomDeathSuicideCause = useRandomDeathSuicideCause;
    }

    public double getPercentageRandomDeathChance() {
        return percentageRandomDeathChance;
    }

    public void setPercentageRandomDeathChance(final double percentageRandomDeathChance) {
        this.percentageRandomDeathChance = percentageRandomDeathChance;
    }

    public double[] getExponentialRandomDeathMaleValues() {
        return exponentialRandomDeathMaleValues;
    }

    public void setExponentialRandomDeathMaleValues(final double... exponentialRandomDeathMaleValues) {
        this.exponentialRandomDeathMaleValues = exponentialRandomDeathMaleValues;
    }

    public double[] getExponentialRandomDeathFemaleValues() {
        return exponentialRandomDeathFemaleValues;
    }

    public void setExponentialRandomDeathFemaleValues(final double... exponentialRandomDeathFemaleValues) {
        this.exponentialRandomDeathFemaleValues = exponentialRandomDeathFemaleValues;
    }

    public Map<TenYearAgeRange, Double> getAgeRangeRandomDeathMaleValues() {
        return ageRangeRandomDeathMaleValues;
    }

    public void setAgeRangeRandomDeathMaleValues(final Map<TenYearAgeRange, Double> ageRangeRandomDeathMaleValues) {
        this.ageRangeRandomDeathMaleValues = ageRangeRandomDeathMaleValues;
    }

    public Map<TenYearAgeRange, Double> getAgeRangeRandomDeathFemaleValues() {
        return ageRangeRandomDeathFemaleValues;
    }

    public void setAgeRangeRandomDeathFemaleValues(final Map<TenYearAgeRange, Double> ageRangeRandomDeathFemaleValues) {
        this.ageRangeRandomDeathFemaleValues = ageRangeRandomDeathFemaleValues;
    }
    //endregion Death
    //endregion Personnel Tab

    //region Finances Tab
    public boolean payForParts() {
        return payForParts;
    }

    public void setPayForParts(boolean b) {
        this.payForParts = b;
    }

    public boolean payForRepairs() {
        return payForRepairs;
    }

    public void setPayForRepairs(boolean b) {
        this.payForRepairs = b;
    }

    public boolean payForUnits() {
        return payForUnits;
    }

    public void setPayForUnits(boolean b) {
        this.payForUnits = b;
    }

    public boolean payForSalaries() {
        return payForSalaries;
    }

    public void setPayForSalaries(boolean b) {
        this.payForSalaries = b;
    }

    public boolean payForOverhead() {
        return payForOverhead;
    }

    public void setPayForOverhead(boolean b) {
        this.payForOverhead = b;
    }

    public boolean payForMaintain() {
        return payForMaintain;
    }

    public void setPayForMaintain(boolean b) {
        this.payForMaintain = b;
    }

    public boolean payForTransport() {
        return payForTransport;
    }

    public void setPayForTransport(boolean b) {
        this.payForTransport = b;
    }

    public boolean canSellUnits() {
        return sellUnits;
    }

    public void setSellUnits(boolean b) {
        this.sellUnits = b;
    }

    public boolean canSellParts() {
        return sellParts;
    }

    public void setSellParts(boolean b) {
        this.sellParts = b;
    }

    public boolean payForRecruitment() {
        return payForRecruitment;
    }

    public void setPayForRecruitment(boolean b) {
        this.payForRecruitment = b;
    }

    public boolean useLoanLimits() {
        return useLoanLimits;
    }

    public void setLoanLimits(boolean b) {
        this.useLoanLimits = b;
    }

    public boolean usePercentageMaint() {
        return usePercentageMaint;
    }

    public void setUsePercentageMaint(boolean b) {
        usePercentageMaint = b;
    }

    public boolean useInfantryDontCount() {
        return infantryDontCount;
    }

    public void setUseInfantryDontCount(boolean b) {
        infantryDontCount = b;
    }

    public boolean usePeacetimeCost() {
        return usePeacetimeCost;
    }

    public void setUsePeacetimeCost(boolean b) {
        this.usePeacetimeCost = b;
    }

    public boolean useExtendedPartsModifier() {
        return useExtendedPartsModifier;
    }

    public void setUseExtendedPartsModifier(boolean b) {
        this.useExtendedPartsModifier = b;
    }

    public boolean showPeacetimeCost() {
        return showPeacetimeCost;
    }

    public void setShowPeacetimeCost(boolean b) {
        this.showPeacetimeCost = b;
    }

    /**
     * @return the duration of a financial year
     */
    public FinancialYearDuration getFinancialYearDuration() {
        return financialYearDuration;
    }

    /**
     * @param financialYearDuration the financial year duration to set
     */
    public void setFinancialYearDuration(FinancialYearDuration financialYearDuration) {
        this.financialYearDuration = financialYearDuration;
    }

    /**
     * @return whether or not to export finances to CSV at the end of a financial year
     */
    public boolean getNewFinancialYearFinancesToCSVExport() {
        return newFinancialYearFinancesToCSVExport;
    }

    /**
     * @param b whether or not to export finances to CSV at the end of a financial year
     */
    public void setNewFinancialYearFinancesToCSVExport(boolean b) {
        newFinancialYearFinancesToCSVExport = b;
    }

    //region Price Multipliers
    public double getCommonPartPriceMultiplier() {
        return commonPartPriceMultiplier;
    }

    public void setCommonPartPriceMultiplier(final double commonPartPriceMultiplier) {
        this.commonPartPriceMultiplier = commonPartPriceMultiplier;
    }

    public double getInnerSphereUnitPriceMultiplier() {
        return innerSphereUnitPriceMultiplier;
    }

    public void setInnerSphereUnitPriceMultiplier(final double innerSphereUnitPriceMultiplier) {
        this.innerSphereUnitPriceMultiplier = innerSphereUnitPriceMultiplier;
    }

    public double getInnerSpherePartPriceMultiplier() {
        return innerSpherePartPriceMultiplier;
    }

    public void setInnerSpherePartPriceMultiplier(final double innerSpherePartPriceMultiplier) {
        this.innerSpherePartPriceMultiplier = innerSpherePartPriceMultiplier;
    }

    public double getClanUnitPriceMultiplier() {
        return clanUnitPriceMultiplier;
    }

    public void setClanUnitPriceMultiplier(final double clanUnitPriceMultiplier) {
        this.clanUnitPriceMultiplier = clanUnitPriceMultiplier;
    }

    public double getClanPartPriceMultiplier() {
        return clanPartPriceMultiplier;
    }

    public void setClanPartPriceMultiplier(final double clanPartPriceMultiplier) {
        this.clanPartPriceMultiplier = clanPartPriceMultiplier;
    }

    public double getMixedTechUnitPriceMultiplier() {
        return mixedTechUnitPriceMultiplier;
    }

    public void setMixedTechUnitPriceMultiplier(final double mixedTechUnitPriceMultiplier) {
        this.mixedTechUnitPriceMultiplier = mixedTechUnitPriceMultiplier;
    }

    public double[] getUsedPartPriceMultipliers() {
        return usedPartPriceMultipliers;
    }

    public void setUsedPartPriceMultipliers(final double... usedPartPriceMultipliers) {
        this.usedPartPriceMultipliers = usedPartPriceMultipliers;
    }

    public double getDamagedPartsValueMultiplier() {
        return damagedPartsValueMultiplier;
    }

    public void setDamagedPartsValueMultiplier(final double damagedPartsValueMultiplier) {
        this.damagedPartsValueMultiplier = damagedPartsValueMultiplier;
    }

    public double getUnrepairablePartsValueMultiplier() {
        return unrepairablePartsValueMultiplier;
    }

    public void setUnrepairablePartsValueMultiplier(final double unrepairablePartsValueMultiplier) {
        this.unrepairablePartsValueMultiplier = unrepairablePartsValueMultiplier;
    }

    public double getCancelledOrderRefundMultiplier() {
        return cancelledOrderRefundMultiplier;
    }

    public void setCancelledOrderRefundMultiplier(final double cancelledOrderRefundMultiplier) {
        this.cancelledOrderRefundMultiplier = cancelledOrderRefundMultiplier;
    }
    //endregion Price Multipliers
    //endregion Finances Tab

    //region Markets Tab
    //region Personnel Market
    public String getPersonnelMarketType() {
        return personnelMarketName;
    }

    public void setPersonnelMarketType(final String personnelMarketName) {
        this.personnelMarketName = personnelMarketName;
    }

    public boolean getPersonnelMarketReportRefresh() {
        return personnelMarketReportRefresh;
    }

    public void setPersonnelMarketReportRefresh(final boolean personnelMarketReportRefresh) {
        this.personnelMarketReportRefresh = personnelMarketReportRefresh;
    }

    public int getPersonnelMarketRandomEliteRemoval() {
        return personnelMarketRandomEliteRemoval;
    }

    public void setPersonnelMarketRandomEliteRemoval(final int personnelMarketRandomEliteRemoval) {
        this.personnelMarketRandomEliteRemoval = personnelMarketRandomEliteRemoval;
    }

    public int getPersonnelMarketRandomVeteranRemoval() {
        return personnelMarketRandomVeteranRemoval;
    }

    public void setPersonnelMarketRandomVeteranRemoval(final int personnelMarketRandomVeteranRemoval) {
        this.personnelMarketRandomVeteranRemoval = personnelMarketRandomVeteranRemoval;
    }

    public int getPersonnelMarketRandomRegularRemoval() {
        return personnelMarketRandomRegularRemoval;
    }

    public void setPersonnelMarketRandomRegularRemoval(final int personnelMarketRandomRegularRemoval) {
        this.personnelMarketRandomRegularRemoval = personnelMarketRandomRegularRemoval;
    }

    public int getPersonnelMarketRandomGreenRemoval() {
        return personnelMarketRandomGreenRemoval;
    }

    public void setPersonnelMarketRandomGreenRemoval(final int personnelMarketRandomGreenRemoval) {
        this.personnelMarketRandomGreenRemoval = personnelMarketRandomGreenRemoval;
    }

    public int getPersonnelMarketRandomUltraGreenRemoval() {
        return personnelMarketRandomUltraGreenRemoval;
    }

    public void setPersonnelMarketRandomUltraGreenRemoval(final int personnelMarketRandomUltraGreenRemoval) {
        this.personnelMarketRandomUltraGreenRemoval = personnelMarketRandomUltraGreenRemoval;
    }

    public double getPersonnelMarketDylansWeight() {
        return personnelMarketDylansWeight;
    }

    public void setPersonnelMarketDylansWeight(final double personnelMarketDylansWeight) {
        this.personnelMarketDylansWeight = personnelMarketDylansWeight;
    }
    //endregion Personnel Market

    //region Unit Market
    public UnitMarketMethod getUnitMarketMethod() {
        return unitMarketMethod;
    }

    public void setUnitMarketMethod(final UnitMarketMethod unitMarketMethod) {
        this.unitMarketMethod = unitMarketMethod;
    }

    public boolean useUnitMarketRegionalMechVariations() {
        return unitMarketRegionalMechVariations;
    }

    public void setUnitMarketRegionalMechVariations(final boolean unitMarketRegionalMechVariations) {
        this.unitMarketRegionalMechVariations = unitMarketRegionalMechVariations;
    }

    public boolean getInstantUnitMarketDelivery() {
        return instantUnitMarketDelivery;
    }

    public void setInstantUnitMarketDelivery(final boolean instantUnitMarketDelivery) {
        this.instantUnitMarketDelivery = instantUnitMarketDelivery;
    }

    public boolean getUnitMarketReportRefresh() {
        return unitMarketReportRefresh;
    }

    public void setUnitMarketReportRefresh(final boolean unitMarketReportRefresh) {
        this.unitMarketReportRefresh = unitMarketReportRefresh;
    }
    //endregion Unit Market

    //region Contract Market
    public ContractMarketMethod getContractMarketMethod() {
        return contractMarketMethod;
    }

    public void setContractMarketMethod(final ContractMarketMethod contractMarketMethod) {
        this.contractMarketMethod = contractMarketMethod;
    }

    public boolean getContractMarketReportRefresh() {
        return contractMarketReportRefresh;
    }

    public void setContractMarketReportRefresh(final boolean contractMarketReportRefresh) {
        this.contractMarketReportRefresh = contractMarketReportRefresh;
    }
    //endregion Contract Market
    //endregion Markets Tab

    //region RATs Tab
    public boolean isUseStaticRATs() {
        return useStaticRATs;
    }

    public void setUseStaticRATs(final boolean useStaticRATs) {
        this.useStaticRATs = useStaticRATs;
    }

    public String[] getRATs() {
        return rats;
    }

    public void setRATs(final String... rats) {
        this.rats = rats;
    }

    public boolean isIgnoreRATEra() {
        return ignoreRATEra;
    }

    public void setIgnoreRATEra(final boolean ignore) {
        ignoreRATEra = ignore;
    }
    //endregion RATs Tab

    public static String getTechLevelName(int lvl) {
        switch (lvl) {
            case TECH_INTRO:
                return TechConstants.T_SIMPLE_NAMES[TechConstants.T_SIMPLE_INTRO];
            case TECH_STANDARD:
                return TechConstants.T_SIMPLE_NAMES[TechConstants.T_SIMPLE_STANDARD];
            case TECH_ADVANCED:
                return TechConstants.T_SIMPLE_NAMES[TechConstants.T_SIMPLE_ADVANCED];
            case TECH_EXPERIMENTAL:
                return TechConstants.T_SIMPLE_NAMES[TechConstants.T_SIMPLE_EXPERIMENTAL];
            case TECH_UNOFFICIAL:
                return TechConstants.T_SIMPLE_NAMES[TechConstants.T_SIMPLE_UNOFFICIAL];
            default:
                return "Unknown";
        }
    }

    public static String getTransitUnitName(int unit) {
        switch (unit) {
            case TRANSIT_UNIT_DAY:
                return "Days";
            case TRANSIT_UNIT_WEEK:
                return "Weeks";
            case TRANSIT_UNIT_MONTH:
                return "Months";
            default:
                return "Unknown";
        }
    }

    public boolean useEraMods() {
        return useEraMods;
    }

    public void setEraMods(boolean b) {
        this.useEraMods = b;
    }

    public boolean useAssignedTechFirst() {
        return assignedTechFirst;
    }

    public void setAssignedTechFirst(boolean assignedTechFirst) {
        this.assignedTechFirst = assignedTechFirst;
    }

    public boolean useResetToFirstTech() {
        return resetToFirstTech;
    }

    public void setResetToFirstTech(boolean resetToFirstTech) {
        this.resetToFirstTech = resetToFirstTech;
    }

    /**
     * @return true to use the origin faction for personnel names instead of a set faction
     */
    public boolean useOriginFactionForNames() {
        return useOriginFactionForNames;
    }

    /**
     * @param useOriginFactionForNames whether to use personnel names or a set faction
     */
    public void setUseOriginFactionForNames(boolean useOriginFactionForNames) {
        this.useOriginFactionForNames = useOriginFactionForNames;
    }

    public boolean useQuirks() {
        return useQuirks;
    }

    public void setQuirks(boolean b) {
        this.useQuirks = b;
    }

    public int getScenarioXP() {
        return scenarioXP;
    }

    public void setScenarioXP(int xp) {
        scenarioXP = xp;
    }

    public int getKillsForXP() {
        return killsForXP;
    }

    public void setKillsForXP(int k) {
        killsForXP = k;
    }

    public int getKillXPAward() {
        return killXPAward;
    }

    public void setKillXPAward(int xp) {
        killXPAward = xp;
    }

    public int getNTasksXP() {
        return nTasksXP;
    }

    public void setNTasksXP(int xp) {
        nTasksXP = xp;
    }

    public int getTaskXP() {
        return tasksXP;
    }

    public void setTaskXP(int b) {
        tasksXP = b;
    }

    public int getMistakeXP() {
        return mistakeXP;
    }

    public void setMistakeXP(int b) {
        mistakeXP = b;
    }

    public int getSuccessXP() {
        return successXP;
    }

    public void setSuccessXP(int b) {
        successXP = b;
    }

    public boolean limitByYear() {
        return limitByYear;
    }

    public void setLimitByYear(boolean b) {
        limitByYear = b;
    }

    public boolean disallowExtinctStuff() {
        return disallowExtinctStuff;
    }

    public void setDisallowExtinctStuff(boolean b) {
        disallowExtinctStuff = b;
    }

    public boolean allowClanPurchases() {
        return allowClanPurchases;
    }

    public void setAllowClanPurchases(boolean b) {
        allowClanPurchases = b;
    }

    public boolean allowISPurchases() {
        return allowISPurchases;
    }

    public void setAllowISPurchases(boolean b) {
        allowISPurchases = b;
    }

    public boolean allowCanonOnly() {
        return allowCanonOnly;
    }

    public void setAllowCanonOnly(boolean b) {
        allowCanonOnly = b;
    }

    public boolean allowCanonRefitOnly() {
        return allowCanonRefitOnly;
    }

    public void setAllowCanonRefitOnly(boolean b) {
        allowCanonRefitOnly = b;
    }

    public boolean useVariableTechLevel() {
        return variableTechLevel;
    }

    public void setVariableTechLevel(boolean b) {
        variableTechLevel = b;
    }

    public void setFactionIntroDate(boolean b) {
        factionIntroDate = b;
    }

    public boolean useFactionIntroDate() {
        return factionIntroDate;
    }

    public boolean useAmmoByType() {
        return useAmmoByType;
    }

    public void setUseAmmoByType(boolean b) {
        useAmmoByType = b;
    }

    public int getTechLevel() {
        return techLevel;
    }

    public void setTechLevel(int lvl) {
        techLevel = lvl;
    }

    public int[] getPhenotypeProbabilities() {
        return phenotypeProbabilities;
    }

    public int getPhenotypeProbability(Phenotype phenotype) {
        return getPhenotypeProbabilities()[phenotype.ordinal()];
    }

    public void setPhenotypeProbability(int index, int percentage) {
        phenotypeProbabilities[index] = percentage;
    }

    public boolean[] usePortraitForRoles() {
        return usePortraitForRole;
    }

    public boolean usePortraitForRole(final PersonnelRole role) {
        return usePortraitForRoles()[role.ordinal()];
    }

    public void setUsePortraitForRole(int index, boolean b) {
        usePortraitForRole[index] = b;
    }

    public boolean getAssignPortraitOnRoleChange() {
        return assignPortraitOnRoleChange;
    }

    public void setAssignPortraitOnRoleChange(boolean b) {
        assignPortraitOnRoleChange = b;
    }

    public int getIdleXP() {
        return idleXP;
    }

    public void setIdleXP(int xp) {
        idleXP = xp;
    }

    public int getTargetIdleXP() {
        return targetIdleXP;
    }

    public void setTargetIdleXP(int xp) {
        targetIdleXP = xp;
    }

    public int getMonthsIdleXP() {
        return monthsIdleXP;
    }

    public void setMonthsIdleXP(int m) {
        monthsIdleXP = m;
    }

    public int getContractNegotiationXP() {
        return contractNegotiationXP;
    }

    public void setContractNegotiationXP(int m) {
        contractNegotiationXP = m;
    }

    public int getAdminXP() {
        return adminXP;
    }

    public void setAdminXP(int m) {
        adminXP = m;
    }

    public int getAdminXPPeriod() {
        return adminXPPeriod;
    }

    public void setAdminXPPeriod(int m) {
        adminXPPeriod = m;
    }

    public int getEdgeCost() {
        return edgeCost;
    }

    public void setEdgeCost(int b) {
        edgeCost = b;
    }

    public int getWaitingPeriod() {
        return waitingPeriod;
    }

    public void setWaitingPeriod(int d) {
        waitingPeriod = d;
    }

    public String getAcquisitionSkill() {
        return acquisitionSkill;
    }

    public void setAcquisitionSkill(String skill) {
        acquisitionSkill = skill;
    }

    public void setAcquisitionSupportStaffOnly(boolean b) {
        this.acquisitionSupportStaffOnly = b;
    }

    public boolean isAcquisitionSupportStaffOnly() {
        return acquisitionSupportStaffOnly;
    }

    public int getNDiceTransitTime() {
        return nDiceTransitTime;
    }

    public void setNDiceTransitTime(int d) {
        nDiceTransitTime = d;
    }

    public int getConstantTransitTime() {
        return constantTransitTime;
    }

    public void setConstantTransitTime(int d) {
        constantTransitTime = d;
    }

    public int getUnitTransitTime() {
        return unitTransitTime;
    }

    public void setUnitTransitTime(int d) {
        unitTransitTime = d;
    }

    public int getAcquireMosUnit() {
        return acquireMosUnit;
    }

    public void setAcquireMosUnit(int b) {
        acquireMosUnit = b;
    }

    public int getAcquireMosBonus() {
        return acquireMosBonus;
    }

    public void setAcquireMosBonus(int b) {
        acquireMosBonus = b;
    }

    public int getAcquireMinimumTimeUnit() {
        return acquireMinimumTimeUnit;
    }

    public void setAcquireMinimumTimeUnit(int b) {
        acquireMinimumTimeUnit = b;
    }

    public int getAcquireMinimumTime() {
        return acquireMinimumTime;
    }

    public void setAcquireMinimumTime(int b) {
        acquireMinimumTime = b;
    }

    public boolean usesPlanetaryAcquisition() {
        return usePlanetaryAcquisition;
    }

    public void setPlanetaryAcquisition(boolean b) {
        usePlanetaryAcquisition = b;
    }

    public PlanetaryAcquisitionFactionLimit getPlanetAcquisitionFactionLimit() {
        return planetAcquisitionFactionLimit;
    }

    public void setPlanetAcquisitionFactionLimit(final PlanetaryAcquisitionFactionLimit planetAcquisitionFactionLimit) {
        this.planetAcquisitionFactionLimit = planetAcquisitionFactionLimit;
    }

    public boolean disallowPlanetAcquisitionClanCrossover() {
        return planetAcquisitionNoClanCrossover;
    }

    public void setDisallowPlanetAcquisitionClanCrossover(boolean b) {
        planetAcquisitionNoClanCrossover = b;
    }

    public int getMaxJumpsPlanetaryAcquisition() {
        return maxJumpsPlanetaryAcquisition;
    }

    public void setMaxJumpsPlanetaryAcquisition(int m) {
        maxJumpsPlanetaryAcquisition = m;
    }

    public int getPenaltyClanPartsFroIS() {
        return penaltyClanPartsFromIS;
    }

    public void setPenaltyClanPartsFroIS(int i) {
        penaltyClanPartsFromIS = i ;
    }

    public boolean disallowClanPartsFromIS() {
        return noClanPartsFromIS;
    }

    public void setDisallowClanPartsFromIS(boolean b) {
        noClanPartsFromIS = b;
    }

    public boolean usePlanetAcquisitionVerboseReporting() {
        return planetAcquisitionVerbose;
    }

    public void setPlanetAcquisitionVerboseReporting(boolean b) {
        planetAcquisitionVerbose = b;
    }

    public double getEquipmentContractPercent() {
        return equipmentContractPercent;
    }

    public void setEquipmentContractPercent(double b) {
        equipmentContractPercent = Math.min(b, MAXIMUM_COMBAT_EQUIPMENT_PERCENT);
    }

    public boolean useEquipmentContractBase() {
        return equipmentContractBase;
    }

    public void setEquipmentContractBase(boolean b) {
        this.equipmentContractBase = b;
    }

    public boolean useEquipmentContractSaleValue() {
        return equipmentContractSaleValue;
    }

    public void setEquipmentContractSaleValue(boolean b) {
        this.equipmentContractSaleValue = b;
    }

    public double getDropshipContractPercent() {
        return dropshipContractPercent;
    }

    public void setDropshipContractPercent(double b) {
        dropshipContractPercent = Math.min(b, MAXIMUM_DROPSHIP_EQUIPMENT_PERCENT);
    }

    public double getJumpshipContractPercent() {
        return jumpshipContractPercent;
    }

    public void setJumpshipContractPercent(double b) {
        jumpshipContractPercent = Math.min(b, MAXIMUM_JUMPSHIP_EQUIPMENT_PERCENT);
    }

    public double getWarshipContractPercent() {
        return warshipContractPercent;
    }

    public void setWarshipContractPercent(double b) {
        warshipContractPercent = Math.min(b, MAXIMUM_WARSHIP_EQUIPMENT_PERCENT);
    }

    public boolean useBLCSaleValue() {
        return blcSaleValue;
    }

    public void setBLCSaleValue(boolean b) {
        this.blcSaleValue = b;
    }

    public boolean getOverageRepaymentInFinalPayment() {
        return overageRepaymentInFinalPayment;
    }

    public void setOverageRepaymentInFinalPayment(boolean overageRepaymentInFinalPayment) {
        this.overageRepaymentInFinalPayment = overageRepaymentInFinalPayment;
    }

    public int getClanAcquisitionPenalty() {
        return clanAcquisitionPenalty;
    }

    public void setClanAcquisitionPenalty(int b) {
        clanAcquisitionPenalty = b;
    }

    public int getIsAcquisitionPenalty() {
        return isAcquisitionPenalty;
    }

    public void setIsAcquisitionPenalty(int b) {
        isAcquisitionPenalty = b;
    }

    public int getPlanetTechAcquisitionBonus(int type) {
        if (type < 0 || type >= planetTechAcquisitionBonus.length) {
            return 0;
        }
        return planetTechAcquisitionBonus[type];
    }

    public void setPlanetTechAcquisitionBonus(int base, int type) {
        if (type < 0 || type >= planetTechAcquisitionBonus.length) {
            return;
        }
        this.planetTechAcquisitionBonus[type] = base;
    }

    public int getPlanetIndustryAcquisitionBonus(int type) {
        if (type < 0 || type >= planetIndustryAcquisitionBonus.length) {
            return 0;
        }
        return planetIndustryAcquisitionBonus[type];
    }

    public void setPlanetIndustryAcquisitionBonus(int base, int type) {
        if (type < 0 || type >= planetIndustryAcquisitionBonus.length) {
            return;
        }
        this.planetIndustryAcquisitionBonus[type] = base;
    }

    public int getPlanetOutputAcquisitionBonus(int type) {
        if (type < 0 || type >= planetOutputAcquisitionBonus.length) {
            return 0;
        }
        return planetOutputAcquisitionBonus[type];
    }

    public void setPlanetOutputAcquisitionBonus(int base, int type) {
        if (type < 0 || type >= planetOutputAcquisitionBonus.length) {
            return;
        }
        this.planetOutputAcquisitionBonus[type] = base;
    }

    public boolean isDestroyByMargin() {
        return destroyByMargin;
    }

    public void setDestroyByMargin(boolean b) {
        destroyByMargin = b;
    }

    public int getDestroyMargin() {
        return destroyMargin;
    }

    public void setDestroyMargin(int d) {
        destroyMargin = d;
    }

    public int getDestroyPartTarget() {
        return destroyPartTarget;
    }

    public void setDestroyPartTarget(int d) {
        destroyPartTarget = d;
    }

    public boolean useAeroSystemHits() {
        return useAeroSystemHits;
    }

    public void setUseAeroSystemHits(boolean b) {
        useAeroSystemHits = b;
    }

    public int getMaxAcquisitions() {
        return maxAcquisitions;
    }

    public void setMaxAcquisitions(int d) {
        maxAcquisitions = d;
    }

    public boolean getUseAtB() {
        return useAtB;
    }

    public void setUseAtB(boolean useAtB) {
        this.useAtB = useAtB;
    }

    public boolean getUseStratCon() {
        return useStratCon;
    }

    public void setUseStratCon(boolean useStratCon) {
        this.useStratCon = useStratCon;
    }

    public boolean getUseAero() {
        return useAero;
    }

    public void setUseAero(boolean useAero) {
        this.useAero = useAero;
    }

    public boolean getUseVehicles() {
        return useVehicles;
    }

    public void setUseVehicles(boolean useVehicles) {
        this.useVehicles = useVehicles;
    }

    public boolean getClanVehicles() {
        return clanVehicles;
    }

    public void setClanVehicles(boolean clanVehicles) {
        this.clanVehicles = clanVehicles;
    }

    public boolean getDoubleVehicles() {
        return doubleVehicles;
    }

    public void setDoubleVehicles(boolean doubleVehicles) {
        this.doubleVehicles = doubleVehicles;
    }

    public boolean getAdjustPlayerVehicles() {
        return adjustPlayerVehicles;
    }

    public int getOpforLanceTypeMechs() {
        return opforLanceTypeMechs;
    }

    public void setOpforLanceTypeMechs(int weight) {
        opforLanceTypeMechs = weight;
    }

    public int getOpforLanceTypeMixed() {
        return opforLanceTypeMixed;
    }

    public void setOpforLanceTypeMixed(int weight) {
        opforLanceTypeMixed = weight;
    }

    public int getOpforLanceTypeVehicles() {
        return opforLanceTypeVehicles;
    }

    public void setOpforLanceTypeVehicles(int weight) {
        opforLanceTypeVehicles = weight;
    }

    public boolean getOpforUsesVTOLs() {
        return opforUsesVTOLs;
    }

    public void setOpforUsesVTOLs(boolean vtol) {
        opforUsesVTOLs = vtol;
    }

    public void setAdjustPlayerVehicles(boolean adjust) {
        adjustPlayerVehicles = adjust;
    }

    public boolean getUseDropShips() {
        return useDropShips;
    }

    public void setUseDropShips(boolean useDropShips) {
        this.useDropShips = useDropShips;
    }

    public int getSkillLevel() {
        return skillLevel;
    }

    public void setSkillLevel(int level) {
        skillLevel = level;
    }

    public boolean getAeroRecruitsHaveUnits() {
        return aeroRecruitsHaveUnits;
    }

    public void setAeroRecruitsHaveUnits(boolean haveUnits) {
        aeroRecruitsHaveUnits = haveUnits;
    }

    public boolean getUseShareSystem() {
        return useShareSystem;
    }

    public boolean getSharesExcludeLargeCraft() {
        return sharesExcludeLargeCraft;
    }

    public void setSharesExcludeLargeCraft(boolean exclude) {
        sharesExcludeLargeCraft = exclude;
    }

    public boolean getSharesForAll() {
        return sharesForAll;
    }

    public void setSharesForAll(boolean set) {
        sharesForAll = set;
    }

    public boolean getTrackOriginalUnit() {
        return trackOriginalUnit;
    }

    public void setTrackOriginalUnit(boolean track) {
        trackOriginalUnit = track;
    }

    public boolean isMercSizeLimited() {
        return mercSizeLimited;
    }

    public void setMercSizeLimited(boolean limit) {
        mercSizeLimited = limit;
    }

    public void setUseShareSystem(boolean shares) {
        useShareSystem = shares;
    }

    public boolean getRegionalMechVariations() {
        return regionalMechVariations;
    }

    public void setRegionalMechVariations(boolean regionalMechVariations) {
        this.regionalMechVariations = regionalMechVariations;
    }

    public boolean getAttachedPlayerCamouflage() {
        return attachedPlayerCamouflage;
    }

    public void setAttachedPlayerCamouflage(boolean attachedPlayerCamouflage) {
        this.attachedPlayerCamouflage = attachedPlayerCamouflage;
    }

    public boolean getPlayerControlsAttachedUnits() {
        return playerControlsAttachedUnits;
    }

    public void setPlayerControlsAttachedUnits(boolean playerControlsAttachedUnits) {
        this.playerControlsAttachedUnits = playerControlsAttachedUnits;
    }

    public int getSearchRadius() {
        return searchRadius;
    }

    public void setSearchRadius(int radius) {
        searchRadius = radius;
    }

    /**
     * @param role the {@link AtBLanceRole} to get the battle chance for
     * @return the chance of having a battle for the specified role
     */
    public int getAtBBattleChance(final AtBLanceRole role) {
        return role.isUnassigned() ? 0 : atbBattleChance[role.ordinal()];
    }

    /**
     * @param role      the {@link AtBLanceRole} ordinal value
     * @param frequency the frequency to set the generation to (percent chance from 0 to 100)
     */
    public void setAtBBattleChance(int role, int frequency) {
        if (frequency < 0) {
            frequency = 0;
        } else if (frequency > 100) {
            frequency = 100;
        }

        this.atbBattleChance[role] = frequency;
    }

    public boolean generateChases() {
        return generateChases;
    }

    public void setGenerateChases(boolean generateChases) {
        this.generateChases = generateChases;
    }

    public boolean getVariableContractLength() {
        return variableContractLength;
    }

    public void setVariableContractLength(boolean variable) {
        variableContractLength = variable;
    }

    public boolean getUseWeatherConditions() {
        return useWeatherConditions;
    }

    public void setUseWeatherConditions(boolean useWeatherConditions) {
        this.useWeatherConditions = useWeatherConditions;
    }

    public boolean getUseLightConditions() {
        return useLightConditions;
    }

    public void setUseLightConditions(boolean useLightConditions) {
        this.useLightConditions = useLightConditions;
    }

    public boolean getUsePlanetaryConditions() {
        return usePlanetaryConditions;
    }

    public void setUsePlanetaryConditions(boolean usePlanetaryConditions) {
        this.usePlanetaryConditions = usePlanetaryConditions;
    }

    public boolean getUseLeadership() {
        return useLeadership;
    }

    public void setUseLeadership(boolean useLeadership) {
        this.useLeadership = useLeadership;
    }

    public boolean getUseStrategy() {
        return useStrategy;
    }

    public void setUseStrategy(boolean useStrategy) {
        this.useStrategy = useStrategy;
    }

    public int getBaseStrategyDeployment() {
        return baseStrategyDeployment;
    }

    public void setBaseStrategyDeployment(int baseStrategyDeployment) {
        this.baseStrategyDeployment = baseStrategyDeployment;
    }

    public int getAdditionalStrategyDeployment() {
        return additionalStrategyDeployment;
    }

    public void setAdditionalStrategyDeployment(int additionalStrategyDeployment) {
        this.additionalStrategyDeployment = additionalStrategyDeployment;
    }

    public boolean getAdjustPaymentForStrategy() {
        return adjustPaymentForStrategy;
    }

    public void setAdjustPaymentForStrategy(boolean adjustPaymentForStrategy) {
        this.adjustPaymentForStrategy = adjustPaymentForStrategy;
    }

    public boolean getRestrictPartsByMission() {
        return restrictPartsByMission;
    }

    public void setRestrictPartsByMission(boolean restrictPartsByMission) {
        this.restrictPartsByMission = restrictPartsByMission;
    }

    public boolean getLimitLanceWeight() {
        return limitLanceWeight;
    }

    public void setLimitLanceWeight(boolean limit) {
        limitLanceWeight = limit;
    }

    public boolean getLimitLanceNumUnits() {
        return limitLanceNumUnits;
    }

    public void setLimitLanceNumUnits(boolean limit) {
        limitLanceNumUnits = limit;
    }

    //region Mass Repair/ Mass Salvage
    public boolean massRepairUseRepair() {
        return massRepairUseRepair;
    }

    public void setMassRepairUseRepair(boolean massRepairUseRepair) {
        this.massRepairUseRepair = massRepairUseRepair;
    }

    public boolean massRepairUseSalvage() {
        return massRepairUseSalvage;
    }

    public void setMassRepairUseSalvage(boolean massRepairUseSalvage) {
        this.massRepairUseSalvage = massRepairUseSalvage;
    }

    public boolean massRepairUseExtraTime() {
        return massRepairUseExtraTime;
    }

    public void setMassRepairUseExtraTime(boolean b) {
        this.massRepairUseExtraTime = b;
    }

    public boolean massRepairUseRushJob() {
        return massRepairUseRushJob;
    }

    public void setMassRepairUseRushJob(boolean b) {
        this.massRepairUseRushJob = b;
    }

    public boolean massRepairAllowCarryover() {
        return massRepairAllowCarryover;
    }

    public void setMassRepairAllowCarryover(boolean b) {
        this.massRepairAllowCarryover = b;
    }

    public boolean massRepairOptimizeToCompleteToday() {
        return massRepairOptimizeToCompleteToday;
    }

    public void setMassRepairOptimizeToCompleteToday(boolean massRepairOptimizeToCompleteToday) {
        this.massRepairOptimizeToCompleteToday = massRepairOptimizeToCompleteToday;
    }

    public boolean massRepairScrapImpossible() {
        return massRepairScrapImpossible;
    }

    public void setMassRepairScrapImpossible(boolean b) {
        this.massRepairScrapImpossible = b;
    }

    public boolean massRepairUseAssignedTechsFirst() {
        return massRepairUseAssignedTechsFirst;
    }

    public void setMassRepairUseAssignedTechsFirst(boolean massRepairUseAssignedTechsFirst) {
        this.massRepairUseAssignedTechsFirst = massRepairUseAssignedTechsFirst;
    }

    public void setMassRepairReplacePod(boolean setMassRepairReplacePod) {
        this.massRepairReplacePod = setMassRepairReplacePod;
    }

    public boolean massRepairReplacePod() {
        return massRepairReplacePod;
    }

    public List<MassRepairOption> getMassRepairOptions() {
        return (massRepairOptions != null) ? massRepairOptions : new ArrayList<>();
    }

    public void setMassRepairOptions(List<MassRepairOption> massRepairOptions) {
        this.massRepairOptions = massRepairOptions;
    }

    public void addMassRepairOption(MassRepairOption mro) {
        if (mro.getType() == PartRepairType.UNKNOWN_LOCATION) {
            return;
        }

        getMassRepairOptions().removeIf(massRepairOption -> massRepairOption.getType() == mro.getType());
        getMassRepairOptions().add(mro);
    }
    //endregion Mass Repair/ Mass Salvage

    public void setAllowOpforAeros(boolean allowOpforAeros) {
        this.allowOpforAeros = allowOpforAeros;
    }

    public boolean getAllowOpforAeros() {
        return allowOpforAeros;
    }

    public void setAllowOpforLocalUnits(boolean allowOpforLocalUnits) {
        this.allowOpforLocalUnits = allowOpforLocalUnits;
    }

    public boolean getAllowOpforLocalUnits() {
        return allowOpforLocalUnits;
    }

    public void setOpforAeroChance(int chance) {
        this.opforAeroChance = chance;
    }

    public int getOpforAeroChance() {
        return opforAeroChance;
    }

    public void setOpforLocalUnitChance(int chance) {
        this.opforLocalUnitChance = chance;
    }

    public int getOpforLocalUnitChance() {
        return opforLocalUnitChance;
    }

    public int getFixedMapChance() {
        return fixedMapChance;
    }

    public void setFixedMapChance(int fixedMapChance) {
        this.fixedMapChance = fixedMapChance;
    }

    public int getSpaUpgradeIntensity() {
        return spaUpgradeIntensity;
    }

    public void setSpaUpgradeIntensity(int spaUpgradeIntensity) {
        this.spaUpgradeIntensity = spaUpgradeIntensity;
    }

    public void writeToXml(PrintWriter pw1, int indent) {
        pw1.println(MHQXMLUtility.indentStr(indent) + "<campaignOptions>");
        //region General Tab
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "manualUnitRatingModifier", getManualUnitRatingModifier());
        //endregion General Tab

        //region Repair and Maintenance Tab
        //region Maintenance
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "logMaintenance", logMaintenance);
        //endregion Maintenance
        //endregion Repair and Maintenance Tab

        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "useFactionForNames", useOriginFactionForNames);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "unitRatingMethod", unitRatingMethod.name());
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "useEraMods", useEraMods);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "assignedTechFirst", assignedTechFirst);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "resetToFirstTech", resetToFirstTech);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "useQuirks", useQuirks);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "scenarioXP", scenarioXP);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "killsForXP", killsForXP);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "killXPAward", killXPAward);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "nTasksXP", nTasksXP);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "tasksXP", tasksXP);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "mistakeXP", mistakeXP);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "successXP", successXP);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "idleXP", idleXP);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "targetIdleXP", targetIdleXP);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "monthsIdleXP", monthsIdleXP);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "contractNegotiationXP", contractNegotiationXP);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "adminWeeklyXP", adminXP);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "adminXPPeriod", adminXPPeriod);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "edgeCost", edgeCost);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "limitByYear", limitByYear);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "disallowExtinctStuff", disallowExtinctStuff);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "allowClanPurchases", allowClanPurchases);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "allowISPurchases", allowISPurchases);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "allowCanonOnly", allowCanonOnly);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "allowCanonRefitOnly", allowCanonRefitOnly);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "variableTechLevel", variableTechLevel);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "factionIntroDate", factionIntroDate);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "useAmmoByType", useAmmoByType);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "waitingPeriod", waitingPeriod);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "acquisitionSkill", acquisitionSkill);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "acquisitionSupportStaffOnly", acquisitionSupportStaffOnly);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "techLevel", techLevel);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "nDiceTransitTime", nDiceTransitTime);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "constantTransitTime", constantTransitTime);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "unitTransitTime", unitTransitTime);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "acquireMosBonus", acquireMosBonus);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "acquireMosUnit", acquireMosUnit);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "acquireMinimumTime", acquireMinimumTime);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "acquireMinimumTimeUnit", acquireMinimumTimeUnit);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "usePlanetaryAcquisition", usePlanetaryAcquisition);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "planetAcquisitionFactionLimit", getPlanetAcquisitionFactionLimit().name());
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "planetAcquisitionNoClanCrossover", planetAcquisitionNoClanCrossover);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "noClanPartsFromIS", noClanPartsFromIS);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "penaltyClanPartsFromIS", penaltyClanPartsFromIS);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "planetAcquisitionVerbose", planetAcquisitionVerbose);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "maxJumpsPlanetaryAcquisition", maxJumpsPlanetaryAcquisition);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "equipmentContractPercent", equipmentContractPercent);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "dropshipContractPercent", dropshipContractPercent);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "jumpshipContractPercent", jumpshipContractPercent);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "warshipContractPercent", warshipContractPercent);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "equipmentContractBase", equipmentContractBase);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "equipmentContractSaleValue", equipmentContractSaleValue);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "blcSaleValue", blcSaleValue);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "overageRepaymentInFinalPayment", overageRepaymentInFinalPayment);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "clanAcquisitionPenalty", clanAcquisitionPenalty);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "isAcquisitionPenalty", isAcquisitionPenalty);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "destroyByMargin", destroyByMargin);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "destroyMargin", destroyMargin);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "destroyPartTarget", destroyPartTarget);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "useAeroSystemHits", useAeroSystemHits);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "maintenanceCycleDays", maintenanceCycleDays);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "maintenanceBonus", maintenanceBonus);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "useQualityMaintenance", useQualityMaintenance);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "reverseQualityNames", reverseQualityNames);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "useUnofficalMaintenance", useUnofficialMaintenance);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "checkMaintenance", checkMaintenance);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "maxAcquisitions", maxAcquisitions);

        //region Personnel Tab
        //region General Personnel
        MHQXMLUtility.writeSimpleXMLTag(pw1, ++indent, "useTactics", useTactics());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "useInitiativeBonus", useInitiativeBonus());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "useToughness", useToughness());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "useArtillery", useArtillery());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "useAbilities", useAbilities());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "useEdge", useEdge());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "useSupportEdge", useSupportEdge());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "useImplants", useImplants());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "alternativeQualityAveraging", useAlternativeQualityAveraging());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "useTransfers", useTransfers());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "useExtendedTOEForceName", isUseExtendedTOEForceName());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "personnelLogSkillGain", isPersonnelLogSkillGain());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "personnelLogAbilityGain", isPersonnelLogAbilityGain());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "personnelLogEdgeGain", isPersonnelLogEdgeGain());
        //endregion General Personnel

        //region Expanded Personnel Information
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "useTimeInService", getUseTimeInService());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "timeInServiceDisplayFormat", getTimeInServiceDisplayFormat().name());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "useTimeInRank", getUseTimeInRank());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "timeInRankDisplayFormat", getTimeInRankDisplayFormat().name());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "trackTotalEarnings", isTrackTotalEarnings());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "trackTotalXPEarnings", isTrackTotalXPEarnings());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "showOriginFaction", showOriginFaction());
        //endregion Expanded Personnel Information

        //region Medical
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "useAdvancedMedical", useAdvancedMedical());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "healWaitingPeriod", getHealingWaitingPeriod());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "naturalHealingWaitingPeriod", getNaturalHealingWaitingPeriod());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "minimumHitsForVehicles", getMinimumHitsForVehicles());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "useRandomHitsForVehicles", useRandomHitsForVehicles());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "tougherHealing", useTougherHealing());
        //endregion Medical

        //region Prisoners
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "prisonerCaptureStyle", getPrisonerCaptureStyle().name());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "defaultPrisonerStatus", getDefaultPrisonerStatus().name());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "prisonerBabyStatus", getPrisonerBabyStatus());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "useAtBPrisonerDefection", useAtBPrisonerDefection());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "useAtBPrisonerRansom", useAtBPrisonerRansom());
        //endregion Prisoners

        //region Personnel Randomization
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "useDylansRandomXP", useDylansRandomXP());
        getRandomOriginOptions().writeToXML(pw1, indent);
        //endregion Personnel Randomization

        //region Retirement
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "useRetirementDateTracking", isUseRetirementDateTracking());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "randomRetirementMethod", getRandomRetirementMethod().name());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "useYearEndRandomRetirement", isUseYearEndRandomRetirement());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "useContractCompletionRandomRetirement", isUseContractCompletionRandomRetirement());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "useCustomRetirementModifiers", isUseCustomRetirementModifiers());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "useRandomFounderRetirement", isUseRandomFounderRetirement());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "trackUnitFatigue", isTrackUnitFatigue());
        //endregion Retirement

        //region Family
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "displayFamilyLevel", getDisplayFamilyLevel().name());
        //endregion Family

        //region Dependent
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "randomDependentMethod", getRandomDependentMethod().name());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "useRandomDependentAddition", isUseRandomDependentAddition());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "useRandomDependentRemoval", isUseRandomDependentsRemoval());
        //endregion Dependent

        //region Salary
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "salaryCommissionMultiplier", getSalaryCommissionMultiplier());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "salaryEnlistedMultiplier", getSalaryEnlistedMultiplier());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "salaryAntiMekMultiplier", getSalaryAntiMekMultiplier());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "salarySpecialistInfantryMultiplier", getSalarySpecialistInfantryMultiplier());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "salaryXPMultiplier", getSalaryXPMultipliers());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "salaryTypeBase", Utilities.printMoneyArray(getRoleBaseSalaries()));
        //endregion Salary

        //region Marriage
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "useManualMarriages", isUseManualMarriages());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "useClannerMarriages", isUseClannerMarriages());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "usePrisonerMarriages", isUsePrisonerMarriages());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "minimumMarriageAge", getMinimumMarriageAge());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "checkMutualAncestorsDepth", getCheckMutualAncestorsDepth());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "logMarriageNameChanges", isLogMarriageNameChanges());
        MHQXMLUtility.writeSimpleXMLOpenTag(pw1, indent++, "marriageSurnameWeights");
        for (final Map.Entry<MergingSurnameStyle, Integer> entry : getMarriageSurnameWeights().entrySet()) {
            MHQXMLUtility.writeSimpleXMLTag(pw1, indent, entry.getKey().name(), entry.getValue());
        }
        MHQXMLUtility.writeSimpleXMLCloseTag(pw1, --indent, "marriageSurnameWeights");
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "randomMarriageMethod", getRandomMarriageMethod().name());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "useRandomSameSexMarriages", isUseRandomSameSexMarriages());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "useRandomClannerMarriages", isUseRandomClannerMarriages());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "useRandomPrisonerMarriages", isUseRandomPrisonerMarriages());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "randomMarriageAgeRange", getRandomMarriageAgeRange());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "percentageRandomMarriageOppositeSexChance", getPercentageRandomMarriageOppositeSexChance());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "percentageRandomMarriageSameSexChance", getPercentageRandomMarriageSameSexChance());
        //endregion Marriage

        //region Divorce
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "useManualDivorce", isUseManualDivorce());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "useClannerDivorce", isUseClannerDivorce());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "usePrisonerDivorce", isUsePrisonerDivorce());
        MHQXMLUtility.writeSimpleXMLOpenTag(pw1, indent++, "divorceSurnameWeights");
        for (final Map.Entry<SplittingSurnameStyle, Integer> entry : getDivorceSurnameWeights().entrySet()) {
            MHQXMLUtility.writeSimpleXMLTag(pw1, indent, entry.getKey().name(), entry.getValue());
        }
        MHQXMLUtility.writeSimpleXMLCloseTag(pw1, --indent, "divorceSurnameWeights");
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "randomDivorceMethod", getRandomDivorceMethod().name());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "useRandomOppositeSexDivorce", isUseRandomOppositeSexDivorce());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "useRandomSameSexDivorce", isUseRandomSameSexDivorce());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "useRandomClannerDivorce", isUseRandomClannerDivorce());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "useRandomPrisonerDivorce", isUseRandomPrisonerDivorce());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "percentageRandomDivorceOppositeSexChance", getPercentageRandomDivorceOppositeSexChance());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "percentageRandomDivorceSameSexChance", getPercentageRandomDivorceSameSexChance());
        //endregion Divorce

        //region Procreation
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "useManualProcreation", isUseManualProcreation());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "useClannerProcreation", isUseClannerProcreation());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "usePrisonerProcreation", isUsePrisonerProcreation());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "multiplePregnancyOccurrences", getMultiplePregnancyOccurrences());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "babySurnameStyle", getBabySurnameStyle().name());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "assignNonPrisonerBabiesFounderTag", isAssignNonPrisonerBabiesFounderTag());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "assignChildrenOfFoundersFounderTag", isAssignChildrenOfFoundersFounderTag());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "determineFatherAtBirth", isDetermineFatherAtBirth());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "displayTrueDueDate", isDisplayTrueDueDate());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "logProcreation", isLogProcreation());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "randomProcreationMethod", getRandomProcreationMethod().name());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "useRelationshiplessRandomProcreation", isUseRelationshiplessRandomProcreation());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "useRandomClannerProcreation", isUseRandomClannerProcreation());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "useRandomPrisonerProcreation", isUseRandomPrisonerProcreation());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "percentageRandomProcreationRelationshipChance", getPercentageRandomProcreationRelationshipChance());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "percentageRandomProcreationRelationshiplessChance", getPercentageRandomProcreationRelationshiplessChance());
        //endregion Procreation

        //region Death
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "keepMarriedNameUponSpouseDeath", getKeepMarriedNameUponSpouseDeath());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "randomDeathMethod", getRandomDeathMethod().name());
        MHQXMLUtility.writeSimpleXMLOpenTag(pw1, indent++, "enabledRandomDeathAgeGroups");
        for (final Entry<AgeGroup, Boolean> entry : getEnabledRandomDeathAgeGroups().entrySet()) {
            MHQXMLUtility.writeSimpleXMLTag(pw1, indent, entry.getKey().name(), entry.getValue());
        }
        MHQXMLUtility.writeSimpleXMLCloseTag(pw1, --indent, "enabledRandomDeathAgeGroups");
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "useRandomClanPersonnelDeath", isUseRandomClanPersonnelDeath());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "useRandomPrisonerDeath", isUseRandomPrisonerDeath());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "useRandomDeathSuicideCause", isUseRandomDeathSuicideCause());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "percentageRandomDeathChance", getPercentageRandomDeathChance());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "exponentialRandomDeathMaleValues", getExponentialRandomDeathMaleValues());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "exponentialRandomDeathFemaleValues", getExponentialRandomDeathFemaleValues());
        MHQXMLUtility.writeSimpleXMLOpenTag(pw1, indent++, "ageRangeRandomDeathMaleValues");
        for (final Entry<TenYearAgeRange, Double> entry : getAgeRangeRandomDeathMaleValues().entrySet()) {
            MHQXMLUtility.writeSimpleXMLTag(pw1, indent, entry.getKey().name(), entry.getValue());
        }
        MHQXMLUtility.writeSimpleXMLCloseTag(pw1, --indent, "ageRangeRandomDeathMaleValues");
        MHQXMLUtility.writeSimpleXMLOpenTag(pw1, indent++, "ageRangeRandomDeathFemaleValues");
        for (final Entry<TenYearAgeRange, Double> entry : getAgeRangeRandomDeathFemaleValues().entrySet()) {
            MHQXMLUtility.writeSimpleXMLTag(pw1, indent, entry.getKey().name(), entry.getValue());
        }
        MHQXMLUtility.writeSimpleXMLCloseTag(pw1, --indent, "ageRangeRandomDeathFemaleValues");
        //endregion Death
        //endregion Personnel Tab

        //region Finances Tab
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent, "payForParts", payForParts);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent, "payForRepairs", payForRepairs);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent, "payForUnits", payForUnits);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent, "payForSalaries", payForSalaries);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent, "payForOverhead", payForOverhead);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent, "payForMaintain", payForMaintain);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent, "payForTransport", payForTransport);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent, "sellUnits", sellUnits);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent, "sellParts", sellParts);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent, "payForRecruitment", payForRecruitment);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent, "useLoanLimits", useLoanLimits);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent, "usePercentageMaint", usePercentageMaint);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent, "infantryDontCount", infantryDontCount);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent, "usePeacetimeCost", usePeacetimeCost);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent, "useExtendedPartsModifier", useExtendedPartsModifier);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent, "showPeacetimeCost", showPeacetimeCost);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent, "financialYearDuration", financialYearDuration.name());
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent, "newFinancialYearFinancesToCSVExport", newFinancialYearFinancesToCSVExport);

        //region Price Multipliers
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent, "commonPartPriceMultiplier", getCommonPartPriceMultiplier());
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent, "innerSphereUnitPriceMultiplier", getInnerSphereUnitPriceMultiplier());
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent, "innerSpherePartPriceMultiplier", getInnerSpherePartPriceMultiplier());
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent, "clanUnitPriceMultiplier", getClanUnitPriceMultiplier());
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent, "clanPartPriceMultiplier", getClanPartPriceMultiplier());
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent, "mixedTechUnitPriceMultiplier", getMixedTechUnitPriceMultiplier());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "usedPartPriceMultipliers", getUsedPartPriceMultipliers());
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent, "damagedPartsValueMultiplier", getDamagedPartsValueMultiplier());
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent, "unrepairablePartsValueMultiplier", getUnrepairablePartsValueMultiplier());
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent, "cancelledOrderRefundMultiplier", getCancelledOrderRefundMultiplier());
        //endregion Price Multipliers
        //endregion Finances Tab

        //region Markets Tab
        //region Personnel Market
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "personnelMarketName", getPersonnelMarketType());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "personnelMarketReportRefresh", getPersonnelMarketReportRefresh());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "personnelMarketRandomEliteRemoval", getPersonnelMarketRandomEliteRemoval());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "personnelMarketRandomVeteranRemoval", getPersonnelMarketRandomVeteranRemoval());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "personnelMarketRandomRegularRemoval", getPersonnelMarketRandomRegularRemoval());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "personnelMarketRandomGreenRemoval", getPersonnelMarketRandomGreenRemoval());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "personnelMarketRandomUltraGreenRemoval", getPersonnelMarketRandomUltraGreenRemoval());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "personnelMarketDylansWeight", getPersonnelMarketDylansWeight());
        //endregion Personnel Market

        //region Unit Market
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "unitMarketMethod", getUnitMarketMethod().name());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "unitMarketRegionalMechVariations", useUnitMarketRegionalMechVariations());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "instantUnitMarketDelivery", getInstantUnitMarketDelivery());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "unitMarketReportRefresh", getUnitMarketReportRefresh());
        //endregion Unit Market

        //region Contract Market
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "contractMarketMethod", getContractMarketMethod().name());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "contractMarketReportRefresh", getContractMarketReportRefresh());
        //endregion Contract Market
        //endregion Markets Tab

        //region RATs Tab
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "useStaticRATs", isUseStaticRATs());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent, "rats", getRATs());
        MHQXMLUtility.writeSimpleXMLTag(pw1, indent--, "ignoreRATEra", isIgnoreRATEra());
        //endregion RATs Tab

        pw1.println(MHQXMLUtility.indentStr(indent + 1)
                + "<phenotypeProbabilities>"
                + StringUtils.join(phenotypeProbabilities, ',')
                + "</phenotypeProbabilities>");
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "useAtB", useAtB);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "useStratCon", useStratCon);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "useAero", useAero);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "useVehicles", useVehicles);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "clanVehicles", clanVehicles);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "doubleVehicles", doubleVehicles);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "adjustPlayerVehicles", adjustPlayerVehicles);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "opforLanceTypeMechs", opforLanceTypeMechs);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "opforLanceTypeMixed", opforLanceTypeMixed);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "opforLanceTypeVehicles", opforLanceTypeVehicles);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "opforUsesVTOLs", opforUsesVTOLs);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "useDropShips", useDropShips);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "skillLevel", skillLevel);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "aeroRecruitsHaveUnits", aeroRecruitsHaveUnits);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "useShareSystem", useShareSystem);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "sharesExcludeLargeCraft", sharesExcludeLargeCraft);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "sharesForAll", sharesForAll);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "mercSizeLimited", mercSizeLimited);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "trackOriginalUnit", trackOriginalUnit);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "regionalMechVariations", regionalMechVariations);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "attachedPlayerCamouflage", attachedPlayerCamouflage);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "playerControlsAttachedUnits", playerControlsAttachedUnits);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "searchRadius", searchRadius);
        pw1.println(MHQXMLUtility.indentStr(indent + 1)
                + "<atbBattleChance>"
                + StringUtils.join(atbBattleChance, ',')
                + "</atbBattleChance>");
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "generateChases", generateChases);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "variableContractLength", variableContractLength);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "useWeatherConditions", useWeatherConditions);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "useLightConditions", useLightConditions);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "usePlanetaryConditions", usePlanetaryConditions);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "useLeadership", useLeadership);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "useStrategy", useStrategy);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "baseStrategyDeployment", baseStrategyDeployment);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "additionalStrategyDeployment", additionalStrategyDeployment);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "adjustPaymentForStrategy", adjustPaymentForStrategy);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "restrictPartsByMission", restrictPartsByMission);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "limitLanceWeight", limitLanceWeight);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "limitLanceNumUnits", limitLanceNumUnits);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "assignPortraitOnRoleChange", assignPortraitOnRoleChange);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "allowOpforAeros", allowOpforAeros);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "allowOpforLocalUnits", allowOpforLocalUnits);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "opforAeroChance", opforAeroChance);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "opforLocalUnitChance", opforLocalUnitChance);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "fixedMapChance", fixedMapChance);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent + 1, "spaUpgradeIntensity", spaUpgradeIntensity);

        //Mass Repair/Salvage Options
        MHQXMLUtility.writeSimpleXmlTag(pw1, ++indent, "massRepairUseRepair", massRepairUseRepair());
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent, "massRepairUseSalvage", massRepairUseSalvage());
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent, "massRepairUseExtraTime", massRepairUseExtraTime);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent, "massRepairUseRushJob", massRepairUseRushJob);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent, "massRepairAllowCarryover", massRepairAllowCarryover);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent, "massRepairOptimizeToCompleteToday", massRepairOptimizeToCompleteToday);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent, "massRepairScrapImpossible", massRepairScrapImpossible);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent, "massRepairUseAssignedTechsFirst", massRepairUseAssignedTechsFirst);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent, "massRepairReplacePod", massRepairReplacePod);

        MHQXMLUtility.writeSimpleXMLOpenIndentedLine(pw1, indent++, "massRepairOptions");
        for (MassRepairOption massRepairOption : massRepairOptions) {
            massRepairOption.writeToXML(pw1, indent);
        }
        MHQXMLUtility.writeSimpleXMLCloseIndentedLine(pw1, --indent, "massRepairOptions");

        pw1.println(MHQXMLUtility.indentStr(indent)
                + "<planetTechAcquisitionBonus>"
                + StringUtils.join(planetTechAcquisitionBonus, ',')
                + "</planetTechAcquisitionBonus>");
        pw1.println(MHQXMLUtility.indentStr(indent)
                + "<planetIndustryAcquisitionBonus>"
                + StringUtils.join(planetIndustryAcquisitionBonus, ',')
                + "</planetIndustryAcquisitionBonus>");
        pw1.println(MHQXMLUtility.indentStr(indent)
                + "<planetOutputAcquisitionBonus>"
                + StringUtils.join(planetOutputAcquisitionBonus, ',')
                + "</planetOutputAcquisitionBonus>");


        // cannot use StringUtils.join for a boolean array, so we are using this instead
        StringBuilder csv = new StringBuilder();
        for (int i = 0; i < usePortraitForRoles().length; i++) {
            csv.append(usePortraitForRoles()[i]);
            if (i < usePortraitForRoles().length - 1) {
                csv.append(",");
            }
        }

        MHQXMLUtility.writeSimpleXmlTag(pw1, indent, "usePortraitForType", csv.toString());
        MHQXMLUtility.writeSimpleXMLCloseTag(pw1, --indent, "campaignOptions");
    }

    public static CampaignOptions generateCampaignOptionsFromXml(Node wn, Version version) {
        LogManager.getLogger().info("Loading Campaign Options from Version " + version + " XML...");

        wn.normalize();
        CampaignOptions retVal = new CampaignOptions();
        NodeList wList = wn.getChildNodes();

        // Okay, lets iterate through the children, eh?
        for (int x = 0; x < wList.getLength(); x++) {
            Node wn2 = wList.item(x);

            // If it's not an element node, we ignore it.
            if (wn2.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            LogManager.getLogger().debug(String.format("%s\n\t%s", wn2.getNodeName(), wn2.getTextContent()));
            try {
                //region Repair and Maintenance Tab
                if (wn2.getNodeName().equalsIgnoreCase("checkMaintenance")) {
                    retVal.checkMaintenance = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("maintenanceCycleDays")) {
                    retVal.maintenanceCycleDays = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("maintenanceBonus")) {
                    retVal.maintenanceBonus = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("useQualityMaintenance")) {
                    retVal.useQualityMaintenance = Boolean.parseBoolean(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("reverseQualityNames")) {
                    retVal.reverseQualityNames = Boolean.parseBoolean(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("useUnofficalMaintenance")) {
                    retVal.useUnofficialMaintenance = Boolean.parseBoolean(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("logMaintenance")) {
                    retVal.logMaintenance = Boolean.parseBoolean(wn2.getTextContent());
                //endregion Repair and Maintenance Tab

                } else if (wn2.getNodeName().equalsIgnoreCase("useFactionForNames")) {
                    retVal.setUseOriginFactionForNames(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("useEraMods")) {
                    retVal.useEraMods = Boolean.parseBoolean(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("assignedTechFirst")) {
                    retVal.assignedTechFirst = Boolean.parseBoolean(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("resetToFirstTech")) {
                    retVal.resetToFirstTech = Boolean.parseBoolean(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("useQuirks")) {
                    retVal.useQuirks = Boolean.parseBoolean(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("scenarioXP")) {
                    retVal.scenarioXP = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("killsForXP")) {
                    retVal.killsForXP = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("killXPAward")) {
                    retVal.killXPAward = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("nTasksXP")) {
                    retVal.nTasksXP = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("tasksXP")) {
                    retVal.tasksXP = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("successXP")) {
                    retVal.successXP = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("mistakeXP")) {
                    retVal.mistakeXP = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("idleXP")) {
                    retVal.idleXP = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("targetIdleXP")) {
                    retVal.targetIdleXP = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("monthsIdleXP")) {
                    retVal.monthsIdleXP = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("contractNegotiationXP")) {
                    retVal.contractNegotiationXP = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("adminWeeklyXP")) {
                    retVal.adminXP = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("adminXPPeriod")) {
                    retVal.adminXPPeriod = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("edgeCost")) {
                    retVal.edgeCost = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("waitingPeriod")) {
                    retVal.waitingPeriod = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("acquisitionSkill")) {
                    retVal.acquisitionSkill = wn2.getTextContent().trim();
                } else if (wn2.getNodeName().equalsIgnoreCase("nDiceTransitTime")) {
                    retVal.nDiceTransitTime = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("constantTransitTime")) {
                    retVal.constantTransitTime = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("unitTransitTime")) {
                    retVal.unitTransitTime = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("acquireMosBonus")) {
                    retVal.acquireMosBonus = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("acquireMosUnit")) {
                    retVal.acquireMosUnit = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("acquireMinimumTime")) {
                    retVal.acquireMinimumTime = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("acquireMinimumTimeUnit")) {
                    retVal.acquireMinimumTimeUnit = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("clanAcquisitionPenalty")) {
                    retVal.clanAcquisitionPenalty = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("isAcquisitionPenalty")) {
                    retVal.isAcquisitionPenalty = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("usePlanetaryAcquisition")) {
                    retVal.usePlanetaryAcquisition = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("planetAcquisitionFactionLimit")) {
                    retVal.setPlanetAcquisitionFactionLimit(PlanetaryAcquisitionFactionLimit.parseFromString(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("planetAcquisitionNoClanCrossover")) {
                    retVal.planetAcquisitionNoClanCrossover = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("noClanPartsFromIS")) {
                    retVal.noClanPartsFromIS = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("penaltyClanPartsFromIS")) {
                    retVal.penaltyClanPartsFromIS = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("planetAcquisitionVerbose")) {
                    retVal.planetAcquisitionVerbose = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("maxJumpsPlanetaryAcquisition")) {
                    retVal.maxJumpsPlanetaryAcquisition = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("planetTechAcquisitionBonus")) {
                    String[] values = wn2.getTextContent().split(",");
                    for (int i = 0; i < values.length; i++) {
                        retVal.planetTechAcquisitionBonus[i] = Integer.parseInt(values[i]);
                    }
                } else if (wn2.getNodeName().equalsIgnoreCase("planetIndustryAcquisitionBonus")) {
                    String[] values = wn2.getTextContent().split(",");
                    for (int i = 0; i < values.length; i++) {
                        retVal.planetIndustryAcquisitionBonus[i] = Integer.parseInt(values[i]);
                    }
                } else if (wn2.getNodeName().equalsIgnoreCase("planetOutputAcquisitionBonus")) {
                    String[] values = wn2.getTextContent().split(",");
                    for (int i = 0; i < values.length; i++) {
                        retVal.planetOutputAcquisitionBonus[i] = Integer.parseInt(values[i]);
                    }
                } else if (wn2.getNodeName().equalsIgnoreCase("equipmentContractPercent")) {
                    retVal.setEquipmentContractPercent(Double.parseDouble(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("dropshipContractPercent")) {
                    retVal.setDropshipContractPercent(Double.parseDouble(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("jumpshipContractPercent")) {
                    retVal.setJumpshipContractPercent(Double.parseDouble(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("warshipContractPercent")) {
                    retVal.setWarshipContractPercent(Double.parseDouble(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("equipmentContractBase")) {
                    retVal.equipmentContractBase = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("equipmentContractSaleValue")) {
                    retVal.equipmentContractSaleValue = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("blcSaleValue")) {
                    retVal.blcSaleValue = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("overageRepaymentInFinalPayment")) {
                    retVal.setOverageRepaymentInFinalPayment(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("acquisitionSupportStaffOnly")) {
                    retVal.acquisitionSupportStaffOnly = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("limitByYear")) {
                    retVal.limitByYear = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("disallowExtinctStuff")) {
                    retVal.disallowExtinctStuff = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("allowClanPurchases")) {
                    retVal.allowClanPurchases = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("allowISPurchases")) {
                    retVal.allowISPurchases = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("allowCanonOnly")) {
                    retVal.allowCanonOnly = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("allowCanonRefitOnly")) {
                    retVal.allowCanonRefitOnly = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("useAmmoByType")) {
                    retVal.useAmmoByType = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("variableTechLevel")) {
                    retVal.variableTechLevel = Boolean.parseBoolean(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("factionIntroDate")) {
                    retVal.factionIntroDate = Boolean.parseBoolean(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("techLevel")) {
                    retVal.techLevel = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("unitRatingMethod")
                        || wn2.getNodeName().equalsIgnoreCase("dragoonsRatingMethod")) {
                    retVal.setUnitRatingMethod(UnitRatingMethod.parseFromString(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("manualUnitRatingModifier")) {
                    retVal.setManualUnitRatingModifier(Integer.parseInt(wn2.getTextContent()));
                } else if (wn2.getNodeName().equalsIgnoreCase("usePortraitForType")) {
                    String[] values = wn2.getTextContent().split(",");
                    if (version.isLowerThan("0.49.0")) {
                        for (int i = 0; i < values.length; i++) {
                            retVal.setUsePortraitForRole(PersonnelRole.parseFromString(String.valueOf(i)).ordinal(),
                                    Boolean.parseBoolean(values[i].trim()));
                        }
                    } else {
                        for (int i = 0; i < values.length; i++) {
                            retVal.setUsePortraitForRole(i, Boolean.parseBoolean(values[i].trim()));
                        }
                    }
                } else if (wn2.getNodeName().equalsIgnoreCase("assignPortraitOnRoleChange")) {
                    retVal.assignPortraitOnRoleChange = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("destroyByMargin")) {
                    retVal.destroyByMargin = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("destroyMargin")) {
                    retVal.destroyMargin = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("destroyPartTarget")) {
                    retVal.destroyPartTarget = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("useAeroSystemHits")) {
                    retVal.useAeroSystemHits = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("maxAcquisitions")) {
                    retVal.maxAcquisitions = Integer.parseInt(wn2.getTextContent().trim());

                //region Personnel Tab
                //region General Personnel
                } else if (wn2.getNodeName().equalsIgnoreCase("useTactics")) {
                    retVal.setUseTactics(Boolean.parseBoolean(wn2.getTextContent()));
                } else if (wn2.getNodeName().equalsIgnoreCase("useInitBonus") // Legacy - 0.49.1 Removal
                        || wn2.getNodeName().equalsIgnoreCase("useInitiativeBonus")) {
                    retVal.setUseInitiativeBonus(Boolean.parseBoolean(wn2.getTextContent()));
                } else if (wn2.getNodeName().equalsIgnoreCase("useToughness")) {
                    retVal.setUseToughness(Boolean.parseBoolean(wn2.getTextContent()));
                } else if (wn2.getNodeName().equalsIgnoreCase("useArtillery")) {
                    retVal.setUseArtillery(Boolean.parseBoolean(wn2.getTextContent()));
                } else if (wn2.getNodeName().equalsIgnoreCase("useAbilities")) {
                    retVal.setUseAbilities(Boolean.parseBoolean(wn2.getTextContent()));
                } else if (wn2.getNodeName().equalsIgnoreCase("useEdge")) {
                    retVal.setUseEdge(Boolean.parseBoolean(wn2.getTextContent()));
                } else if (wn2.getNodeName().equalsIgnoreCase("useSupportEdge")) {
                    retVal.setUseSupportEdge(Boolean.parseBoolean(wn2.getTextContent()));
                } else if (wn2.getNodeName().equalsIgnoreCase("useImplants")) {
                    retVal.setUseImplants(Boolean.parseBoolean(wn2.getTextContent()));
                } else if (wn2.getNodeName().equalsIgnoreCase("altQualityAveraging") // Legacy - 0.49.1 Removal
                        || wn2.getNodeName().equalsIgnoreCase("alternativeQualityAveraging")) {
                    retVal.setAlternativeQualityAveraging(Boolean.parseBoolean(wn2.getTextContent()));
                } else if (wn2.getNodeName().equalsIgnoreCase("useTransfers")) {
                    retVal.setUseTransfers(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("useExtendedTOEForceName")) {
                    retVal.setUseExtendedTOEForceName(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("personnelLogSkillGain")) {
                    retVal.setPersonnelLogSkillGain(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("personnelLogAbilityGain")) {
                    retVal.setPersonnelLogAbilityGain(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("personnelLogEdgeGain")) {
                    retVal.setPersonnelLogEdgeGain(Boolean.parseBoolean(wn2.getTextContent().trim()));
                //endregion General Personnel

                //region Expanded Personnel Information
                } else if (wn2.getNodeName().equalsIgnoreCase("useTimeInService")) {
                    retVal.setUseTimeInService(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("timeInServiceDisplayFormat")) {
                    retVal.setTimeInServiceDisplayFormat(TimeInDisplayFormat.valueOf(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("useTimeInRank")) {
                    retVal.setUseTimeInRank(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("timeInRankDisplayFormat")) {
                    retVal.setTimeInRankDisplayFormat(TimeInDisplayFormat.valueOf(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("trackTotalEarnings")) {
                    retVal.setTrackTotalEarnings(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("trackTotalXPEarnings")) {
                    retVal.setTrackTotalXPEarnings(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("showOriginFaction")) {
                    retVal.setShowOriginFaction(Boolean.parseBoolean(wn2.getTextContent()));
                //endregion Expanded Personnel Information

                //region Medical
                } else if (wn2.getNodeName().equalsIgnoreCase("useAdvancedMedical")) {
                    retVal.setUseAdvancedMedical(Boolean.parseBoolean(wn2.getTextContent()));
                } else if (wn2.getNodeName().equalsIgnoreCase("healWaitingPeriod")) {
                    retVal.setHealingWaitingPeriod(Integer.parseInt(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("naturalHealingWaitingPeriod")) {
                    retVal.setNaturalHealingWaitingPeriod(Integer.parseInt(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("minimumHitsForVees") // Legacy - 0.49.1 Removal
                        || wn2.getNodeName().equalsIgnoreCase("minimumHitsForVehicles")) {
                    retVal.setMinimumHitsForVehicles(Integer.parseInt(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("useRandomHitsForVees") // Legacy - 0.49.1 Removal
                        || wn2.getNodeName().equalsIgnoreCase("useRandomHitsForVehicles")) {
                    retVal.setUseRandomHitsForVehicles(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("tougherHealing")) {
                    retVal.setTougherHealing(Boolean.parseBoolean(wn2.getTextContent().trim()));
                //endregion Medical

                //region Prisoners
                } else if (wn2.getNodeName().equalsIgnoreCase("prisonerCaptureStyle")) {
                    retVal.setPrisonerCaptureStyle(PrisonerCaptureStyle.valueOf(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("defaultPrisonerStatus")) {
                    // Most of this is legacy - 0.47.X Removal
                    String prisonerStatus = wn2.getTextContent().trim();

                    try {
                        prisonerStatus = String.valueOf(Integer.parseInt(prisonerStatus) + 1);
                    } catch (Exception ignored) {

                    }

                    retVal.setDefaultPrisonerStatus(PrisonerStatus.parseFromString(prisonerStatus));
                } else if (wn2.getNodeName().equalsIgnoreCase("prisonerBabyStatus")) {
                    retVal.setPrisonerBabyStatus(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("useAtBPrisonerDefection")) {
                    retVal.setUseAtBPrisonerDefection(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("useAtBPrisonerRansom")) {
                    retVal.setUseAtBPrisonerRansom(Boolean.parseBoolean(wn2.getTextContent().trim()));
                //endregion Prisoners

                //region Personnel Randomization
                } else if (wn2.getNodeName().equalsIgnoreCase("useDylansRandomXP")) {
                    retVal.setUseDylansRandomXP(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("randomOriginOptions")) {
                    if (!wn2.hasChildNodes()) {
                        continue;
                    }
                    final RandomOriginOptions randomOriginOptions = RandomOriginOptions.parseFromXML(wn2.getChildNodes(), true);
                    if (randomOriginOptions == null) {
                        continue;
                    }
                    retVal.setRandomOriginOptions(randomOriginOptions);
                //endregion Personnel Randomization

                //region Retirement
                } else if (wn2.getNodeName().equalsIgnoreCase("useRetirementDateTracking")) {
                    retVal.setUseRetirementDateTracking(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("randomRetirementMethod")) {
                    retVal.setRandomRetirementMethod(RandomRetirementMethod.valueOf(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("useYearEndRandomRetirement")) {
                    retVal.setUseYearEndRandomRetirement(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("useContractCompletionRandomRetirement")) {
                    retVal.setUseContractCompletionRandomRetirement(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("useCustomRetirementModifiers")) {
                    retVal.setUseCustomRetirementModifiers(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("useRandomFounderRetirement")) {
                    retVal.setUseRandomFounderRetirement(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("trackUnitFatigue")) {
                    retVal.setTrackUnitFatigue(Boolean.parseBoolean(wn2.getTextContent().trim()));
                //endregion Retirement

                //region Family
                } else if (wn2.getNodeName().equalsIgnoreCase("displayFamilyLevel")) {
                    retVal.setDisplayFamilyLevel(FamilialRelationshipDisplayLevel.parseFromString(wn2.getTextContent().trim()));
                //endregion Family

                //region Dependent
                } else if (wn2.getNodeName().equalsIgnoreCase("randomDependentMethod")) {
                    retVal.setRandomDependentMethod(RandomDependentMethod.valueOf(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("useRandomDependentAddition")) {
                    retVal.setUseRandomDependentAddition(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("useRandomDependentRemoval")) {
                    retVal.setUseRandomDependentRemoval(Boolean.parseBoolean(wn2.getTextContent().trim()));
                //endregion Dependent

                //region Salary
                } else if (wn2.getNodeName().equalsIgnoreCase("salaryCommissionMultiplier")) {
                    retVal.setSalaryCommissionMultiplier(Double.parseDouble(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("salaryEnlistedMultiplier")) {
                    retVal.setSalaryEnlistedMultiplier(Double.parseDouble(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("salaryAntiMekMultiplier")) {
                    retVal.setSalaryAntiMekMultiplier(Double.parseDouble(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("salarySpecialistInfantryMultiplier")) {
                    retVal.setSalarySpecialistInfantryMultiplier(Double.parseDouble(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("salaryXPMultiplier")) {
                    String[] values = wn2.getTextContent().split(",");
                    for (int i = 0; i < values.length; i++) {
                        retVal.setSalaryXPMultiplier(i, Double.parseDouble(values[i]));
                    }
                } else if (wn2.getNodeName().equalsIgnoreCase("salaryTypeBase")) {
                    if (version.isLowerThan("0.49.0")) {
                        Money[] roleBaseSalaries = Utilities.readMoneyArray(wn2);
                        for (int i = 0; i < roleBaseSalaries.length; i++) {
                            retVal.setRoleBaseSalary(PersonnelRole.parseFromString(String.valueOf(i)), roleBaseSalaries[i]);
                        }
                    } else {
                        retVal.setRoleBaseSalaries(Utilities.readMoneyArray(wn2, retVal.getRoleBaseSalaries().length));
                    }
                //endregion Salary

                //region Marriage
                } else if (wn2.getNodeName().equalsIgnoreCase("useManualMarriages")) {
                    retVal.setUseManualMarriages(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("useClannerMarriages")) {
                    retVal.setUseClannerMarriages(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("usePrisonerMarriages")) {
                    retVal.setUsePrisonerMarriages(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("minimumMarriageAge")) {
                    retVal.setMinimumMarriageAge(Integer.parseInt(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("checkMutualAncestorsDepth")) {
                    retVal.setCheckMutualAncestorsDepth(Integer.parseInt(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("logMarriageNameChanges")) {
                    retVal.setLogMarriageNameChanges(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("marriageSurnameWeights")) {
                    if (!wn2.hasChildNodes()) {
                        continue;
                    }
                    final NodeList nl2 = wn2.getChildNodes();
                    for (int j = 0; j < nl2.getLength(); j++) {
                        final Node wn3 = nl2.item(j);
                        if (wn3.getNodeType() != Node.ELEMENT_NODE) {
                            continue;
                        }
                        retVal.getMarriageSurnameWeights().put(
                                MergingSurnameStyle.parseFromString(wn3.getNodeName().trim()),
                                Integer.parseInt(wn3.getTextContent().trim()));
                    }
                } else if (wn2.getNodeName().equalsIgnoreCase("randomMarriageMethod")) {
                    retVal.setRandomMarriageMethod(RandomMarriageMethod.valueOf(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("useRandomSameSexMarriages")) {
                    retVal.setUseRandomSameSexMarriages(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("useRandomClannerMarriages")) {
                    retVal.setUseRandomClannerMarriages(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("useRandomPrisonerMarriages")) {
                    retVal.setUseRandomPrisonerMarriages(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("randomMarriageAgeRange")) {
                    retVal.setRandomMarriageAgeRange(Integer.parseInt(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("percentageRandomMarriageOppositeSexChance")) {
                    retVal.setPercentageRandomMarriageOppositeSexChance(Double.parseDouble(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("percentageRandomMarriageSameSexChance")) {
                    retVal.setPercentageRandomMarriageSameSexChance(Double.parseDouble(wn2.getTextContent().trim()));
                //endregion Marriage

                //region Divorce
                } else if (wn2.getNodeName().equalsIgnoreCase("useManualDivorce")) {
                    retVal.setUseManualDivorce(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("useClannerDivorce")) {
                    retVal.setUseClannerDivorce(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("usePrisonerDivorce")) {
                    retVal.setUsePrisonerDivorce(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("divorceSurnameWeights")) {
                    if (!wn2.hasChildNodes()) {
                        continue;
                    }
                    final NodeList nl2 = wn2.getChildNodes();
                    for (int j = 0; j < nl2.getLength(); j++) {
                        final Node wn3 = nl2.item(j);
                        if (wn3.getNodeType() != Node.ELEMENT_NODE) {
                            continue;
                        }
                        retVal.getDivorceSurnameWeights().put(
                                SplittingSurnameStyle.valueOf(wn3.getNodeName().trim()),
                                Integer.parseInt(wn3.getTextContent().trim()));
                    }
                } else if (wn2.getNodeName().equalsIgnoreCase("randomDivorceMethod")) {
                    retVal.setRandomDivorceMethod(RandomDivorceMethod.valueOf(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("useRandomOppositeSexDivorce")) {
                    retVal.setUseRandomOppositeSexDivorce(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("useRandomSameSexDivorce")) {
                    retVal.setUseRandomSameSexDivorce(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("useRandomClannerDivorce")) {
                    retVal.setUseRandomClannerDivorce(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("useRandomPrisonerDivorce")) {
                    retVal.setUseRandomPrisonerDivorce(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("percentageRandomDivorceOppositeSexChance")) {
                    retVal.setPercentageRandomDivorceOppositeSexChance(Double.parseDouble(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("percentageRandomDivorceSameSexChance")) {
                    retVal.setPercentageRandomDivorceSameSexChance(Double.parseDouble(wn2.getTextContent().trim()));
                //endregion Divorce

                //region Procreation
                } else if (wn2.getNodeName().equalsIgnoreCase("useManualProcreation")) {
                    retVal.setUseManualProcreation(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("useClannerProcreation")) {
                    retVal.setUseClannerProcreation(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("usePrisonerProcreation")) {
                    retVal.setUsePrisonerProcreation(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("multiplePregnancyOccurrences")) {
                    retVal.setMultiplePregnancyOccurrences(Integer.parseInt(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("babySurnameStyle")) {
                    retVal.setBabySurnameStyle(BabySurnameStyle.parseFromString(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("assignNonPrisonerBabiesFounderTag")) {
                    retVal.setAssignNonPrisonerBabiesFounderTag(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("assignChildrenOfFoundersFounderTag")) {
                    retVal.setAssignChildrenOfFoundersFounderTag(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("determineFatherAtBirth")) {
                    retVal.setDetermineFatherAtBirth(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("displayTrueDueDate")) {
                    retVal.setDisplayTrueDueDate(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("logProcreation")) {
                    retVal.setLogProcreation(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("randomProcreationMethod")) {
                    retVal.setRandomProcreationMethod(RandomProcreationMethod.valueOf(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("useRelationshiplessRandomProcreation")) {
                    retVal.setUseRelationshiplessRandomProcreation(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("useRandomClannerProcreation")) {
                    retVal.setUseRandomClannerProcreation(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("useRandomPrisonerProcreation")) {
                    retVal.setUseRandomPrisonerProcreation(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("percentageRandomProcreationRelationshipChance")) {
                    retVal.setPercentageRandomProcreationRelationshipChance(Double.parseDouble(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("percentageRandomProcreationRelationshiplessChance")) {
                    retVal.setPercentageRandomProcreationRelationshiplessChance(Double.parseDouble(wn2.getTextContent().trim()));
                //endregion Procreation

                //region Death
                } else if (wn2.getNodeName().equalsIgnoreCase("keepMarriedNameUponSpouseDeath")) {
                    retVal.setKeepMarriedNameUponSpouseDeath(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("randomDeathMethod")) {
                    retVal.setRandomDeathMethod(RandomDeathMethod.valueOf(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("enabledRandomDeathAgeGroups")) {
                    if (!wn2.hasChildNodes()) {
                        continue;
                    }
                    final NodeList nl2 = wn2.getChildNodes();
                    for (int i = 0; i < nl2.getLength(); i++) {
                        final Node wn3 = nl2.item(i);
                        try {
                            retVal.getEnabledRandomDeathAgeGroups().put(
                                    AgeGroup.valueOf(wn3.getNodeName()),
                                    Boolean.parseBoolean(wn3.getTextContent().trim()));
                        } catch (Exception ignored) {

                        }
                    }
                } else if (wn2.getNodeName().equalsIgnoreCase("useRandomClanPersonnelDeath")) {
                    retVal.setUseRandomClanPersonnelDeath(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("useRandomPrisonerDeath")) {
                    retVal.setUseRandomPrisonerDeath(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("useRandomDeathSuicideCause")) {
                    retVal.setUseRandomDeathSuicideCause(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("percentageRandomDeathChance")) {
                    retVal.setPercentageRandomDeathChance(Double.parseDouble(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("exponentialRandomDeathMaleValues")) {
                    final String[] values = wn2.getTextContent().trim().split(",");
                    retVal.setExponentialRandomDeathMaleValues(Arrays.stream(values)
                            .mapToDouble(Double::parseDouble)
                            .toArray());
                } else if (wn2.getNodeName().equalsIgnoreCase("exponentialRandomDeathFemaleValues")) {
                    final String[] values = wn2.getTextContent().trim().split(",");
                    retVal.setExponentialRandomDeathFemaleValues(Arrays.stream(values)
                            .mapToDouble(Double::parseDouble)
                            .toArray());
                } else if (wn2.getNodeName().equalsIgnoreCase("ageRangeRandomDeathMaleValues")) {
                    if (!wn2.hasChildNodes()) {
                        continue;
                    }
                    final NodeList nl2 = wn2.getChildNodes();
                    for (int i = 0; i < nl2.getLength(); i++) {
                        final Node wn3 = nl2.item(i);
                        try {
                            retVal.getAgeRangeRandomDeathMaleValues().put(
                                    TenYearAgeRange.valueOf(wn3.getNodeName()),
                                    Double.parseDouble(wn3.getTextContent().trim()));
                        } catch (Exception ignored) {

                        }
                    }
                } else if (wn2.getNodeName().equalsIgnoreCase("ageRangeRandomDeathFemaleValues")) {
                    if (!wn2.hasChildNodes()) {
                        continue;
                    }
                    final NodeList nl2 = wn2.getChildNodes();
                    for (int i = 0; i < nl2.getLength(); i++) {
                        final Node wn3 = nl2.item(i);
                        try {
                            retVal.getAgeRangeRandomDeathFemaleValues().put(
                                    TenYearAgeRange.valueOf(wn3.getNodeName()),
                                    Double.parseDouble(wn3.getTextContent().trim()));
                        } catch (Exception ignored) {

                        }
                    }
                //endregion Death
                //endregion Personnel Tab

                //region Finances Tab
                } else if (wn2.getNodeName().equalsIgnoreCase("payForParts")) {
                    retVal.payForParts = Boolean.parseBoolean(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("payForRepairs")) {
                    retVal.payForRepairs = Boolean.parseBoolean(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("payForUnits")) {
                    retVal.payForUnits = Boolean.parseBoolean(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("payForSalaries")) {
                    retVal.payForSalaries = Boolean.parseBoolean(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("payForOverhead")) {
                    retVal.payForOverhead = Boolean.parseBoolean(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("payForMaintain")) {
                    retVal.payForMaintain = Boolean.parseBoolean(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("payForTransport")) {
                    retVal.payForTransport = Boolean.parseBoolean(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("sellUnits")) {
                    retVal.sellUnits = Boolean.parseBoolean(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("sellParts")) {
                    retVal.sellParts = Boolean.parseBoolean(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("payForRecruitment")) {
                    retVal.payForRecruitment = Boolean.parseBoolean(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("useLoanLimits")) {
                    retVal.useLoanLimits = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("usePercentageMaint")) {
                    retVal.usePercentageMaint = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("infantryDontCount")) {
                    retVal.infantryDontCount = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("usePeacetimeCost")) {
                    retVal.usePeacetimeCost = Boolean.parseBoolean(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("useExtendedPartsModifier")) {
                    retVal.useExtendedPartsModifier = Boolean.parseBoolean(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("showPeacetimeCost")) {
                    retVal.showPeacetimeCost = Boolean.parseBoolean(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("financialYearDuration")) {
                    retVal.setFinancialYearDuration(FinancialYearDuration.parseFromString(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("newFinancialYearFinancesToCSVExport")) {
                    retVal.newFinancialYearFinancesToCSVExport = Boolean.parseBoolean(wn2.getTextContent().trim());

                //region Price Multipliers
                } else if (wn2.getNodeName().equalsIgnoreCase("commonPartPriceMultiplier")) {
                    retVal.setCommonPartPriceMultiplier(Double.parseDouble(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("innerSphereUnitPriceMultiplier")) {
                    retVal.setInnerSphereUnitPriceMultiplier(Double.parseDouble(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("innerSpherePartPriceMultiplier")) {
                    retVal.setInnerSpherePartPriceMultiplier(Double.parseDouble(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("clanUnitPriceMultiplier")) {
                    retVal.setClanUnitPriceMultiplier(Double.parseDouble(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("clanPartPriceMultiplier")) {
                    retVal.setClanPartPriceMultiplier(Double.parseDouble(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("mixedTechUnitPriceMultiplier")) {
                    retVal.setMixedTechUnitPriceMultiplier(Double.parseDouble(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("usedPartPriceMultipliers")) {
                    final String[] values = wn2.getTextContent().split(",");
                    for (int i = 0; i < values.length; i++) {
                        try {
                            retVal.getUsedPartPriceMultipliers()[i] = Double.parseDouble(values[i]);
                        } catch (Exception ignored) {

                        }
                    }
                } else if (wn2.getNodeName().equalsIgnoreCase("damagedPartsValueMultiplier")) {
                    retVal.setDamagedPartsValueMultiplier(Double.parseDouble(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("unrepairablePartsValueMultiplier")) {
                    retVal.setUnrepairablePartsValueMultiplier(Double.parseDouble(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("cancelledOrderRefundMultiplier")) {
                    retVal.setCancelledOrderRefundMultiplier(Double.parseDouble(wn2.getTextContent().trim()));
                //endregion Price Multipliers
                //endregion Finances Tab

                //region Markets Tab
                //region Personnel Market
                } else if (wn2.getNodeName().equalsIgnoreCase("personnelMarketType")) { // Legacy - pre-0.48
                    retVal.setPersonnelMarketType(PersonnelMarket.getTypeName(Integer.parseInt(wn2.getTextContent().trim())));
                } else if (wn2.getNodeName().equalsIgnoreCase("personnelMarketName")) {
                    retVal.setPersonnelMarketType(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("personnelMarketReportRefresh")) {
                    retVal.setPersonnelMarketReportRefresh(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("personnelMarketRandomEliteRemoval")) {
                    retVal.setPersonnelMarketRandomEliteRemoval(Integer.parseInt(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("personnelMarketRandomVeteranRemoval")) {
                    retVal.setPersonnelMarketRandomVeteranRemoval(Integer.parseInt(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("personnelMarketRandomRegularRemoval")) {
                    retVal.setPersonnelMarketRandomRegularRemoval(Integer.parseInt(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("personnelMarketRandomGreenRemoval")) {
                    retVal.setPersonnelMarketRandomGreenRemoval(Integer.parseInt(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("personnelMarketRandomUltraGreenRemoval")) {
                    retVal.setPersonnelMarketRandomUltraGreenRemoval(Integer.parseInt(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("personnelMarketDylansWeight")) {
                    retVal.setPersonnelMarketDylansWeight(Double.parseDouble(wn2.getTextContent().trim()));
                //endregion Personnel Market

                //region Unit Market
                } else if (wn2.getNodeName().equalsIgnoreCase("unitMarketMethod")) {
                    retVal.setUnitMarketMethod(UnitMarketMethod.valueOf(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("unitMarketRegionalMechVariations")) {
                    retVal.setUnitMarketRegionalMechVariations(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("instantUnitMarketDelivery")) {
                    retVal.setInstantUnitMarketDelivery(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("unitMarketReportRefresh")) {
                    retVal.setUnitMarketReportRefresh(Boolean.parseBoolean(wn2.getTextContent().trim()));
                //endregion Unit Market

                //region Contract Market
                } else if (wn2.getNodeName().equalsIgnoreCase("contractMarketMethod")) {
                    retVal.setContractMarketMethod(ContractMarketMethod.valueOf(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("contractMarketReportRefresh")) {
                    retVal.setContractMarketReportRefresh(Boolean.parseBoolean(wn2.getTextContent().trim()));
                //endregion Contract Market
                //endregion Markets Tab

                //region RATs Tab
                } else if (wn2.getNodeName().equals("useStaticRATs")) {
                    retVal.setUseStaticRATs(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("rats")) {
                    retVal.setRATs(MHQXMLUtility.unEscape(wn2.getTextContent().trim()).split(","));
                } else if (wn2.getNodeName().equals("ignoreRATEra")) {
                    retVal.setIgnoreRATEra(Boolean.parseBoolean(wn2.getTextContent().trim()));
                //endregion RATs Tab

                } else if (wn2.getNodeName().equalsIgnoreCase("phenotypeProbabilities")) {
                    String[] values = wn2.getTextContent().split(",");
                    for (int i = 0; i < values.length; i++) {
                        retVal.phenotypeProbabilities[i] = Integer.parseInt(values[i]);
                    }
                } else if (wn2.getNodeName().equalsIgnoreCase("useAtB")) {
                    retVal.useAtB = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("useStratCon")) {
                    retVal.useStratCon = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("useAero")) {
                    retVal.useAero = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("useVehicles")) {
                    retVal.useVehicles = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("clanVehicles")) {
                    retVal.clanVehicles = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("doubleVehicles")) {
                    retVal.doubleVehicles = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("adjustPlayerVehicles")) {
                    retVal.adjustPlayerVehicles = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("opforLanceTypeMechs")) {
                    retVal.opforLanceTypeMechs = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("opforLanceTypeMixed")) {
                    retVal.opforLanceTypeMixed = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("opforLanceTypeVehicles")) {
                    retVal.opforLanceTypeVehicles = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("opforUsesVTOLs")) {
                    retVal.opforUsesVTOLs = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("useDropShips")) {
                    retVal.useDropShips = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("skillLevel")) {
                    retVal.skillLevel = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("aeroRecruitsHaveUnits")) {
                    retVal.aeroRecruitsHaveUnits = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("useShareSystem")) {
                    retVal.useShareSystem = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("sharesExcludeLargeCraft")) {
                    retVal.sharesExcludeLargeCraft = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("sharesForAll")) {
                    retVal.sharesForAll = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("trackOriginalUnit")) {
                    retVal.trackOriginalUnit = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("mercSizeLimited")) {
                    retVal.mercSizeLimited = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("regionalMechVariations")) {
                    retVal.regionalMechVariations = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("attachedPlayerCamouflage")) {
                    retVal.attachedPlayerCamouflage = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("playerControlsAttachedUnits")) {
                    retVal.setPlayerControlsAttachedUnits(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("searchRadius")) {
                    retVal.searchRadius = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("atbBattleChance")) {
                    String[] values = wn2.getTextContent().split(",");
                    for (int i = 0; i < values.length; i++) {
                        try {
                            retVal.atbBattleChance[i] = Integer.parseInt(values[i]);
                        } catch (Exception ignored) {
                            // Badly coded, but this is to migrate devs and their games as the swap was
                            // done before a release and is thus better to handle this way than through
                            // a more code complex method
                            retVal.atbBattleChance[i] = (int) Math.round(Double.parseDouble(values[i]));
                        }
                    }
                } else if (wn2.getNodeName().equalsIgnoreCase("generateChases")) {
                    retVal.setGenerateChases(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("variableContractLength")) {
                    retVal.variableContractLength = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("useWeatherConditions")) {
                    retVal.useWeatherConditions = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("useLightConditions")) {
                    retVal.useLightConditions = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("usePlanetaryConditions")) {
                    retVal.usePlanetaryConditions = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("useLeadership")) {
                    retVal.useLeadership = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("useStrategy")) {
                    retVal.useStrategy = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("baseStrategyDeployment")) {
                    retVal.baseStrategyDeployment = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("additionalStrategyDeployment")) {
                    retVal.additionalStrategyDeployment = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("adjustPaymentForStrategy")) {
                    retVal.adjustPaymentForStrategy = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("restrictPartsByMission")) {
                    retVal.restrictPartsByMission = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("limitLanceWeight")) {
                    retVal.limitLanceWeight = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("limitLanceNumUnits")) {
                    retVal.limitLanceNumUnits = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("allowOpforLocalUnits")) {
                    retVal.allowOpforLocalUnits = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("allowOpforAeros")) {
                    retVal.allowOpforAeros = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("opforAeroChance")) {
                    retVal.opforAeroChance = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("opforLocalUnitChance")) {
                    retVal.opforLocalUnitChance = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("fixedMapChance")) {
                    retVal.fixedMapChance = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("spaUpgradeIntensity")) {
                    retVal.setSpaUpgradeIntensity(Integer.parseInt(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("massRepairUseRepair")) {
                    retVal.setMassRepairUseRepair(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("massRepairUseSalvage")) {
                    retVal.setMassRepairUseSalvage(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("massRepairUseExtraTime")) {
                    retVal.massRepairUseExtraTime = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("massRepairUseRushJob")) {
                    retVal.massRepairUseRushJob = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("massRepairAllowCarryover")) {
                    retVal.massRepairAllowCarryover = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("massRepairOptimizeToCompleteToday")) {
                    retVal.massRepairOptimizeToCompleteToday = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("massRepairScrapImpossible")) {
                    retVal.massRepairScrapImpossible = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("massRepairUseAssignedTechsFirst")) {
                    retVal.massRepairUseAssignedTechsFirst = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("massRepairReplacePod")) {
                    retVal.massRepairReplacePod = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("massRepairOptions")) {
                    retVal.setMassRepairOptions(MassRepairOption.parseListFromXML(wn2, version));

                //region Legacy
                // Removed in 0.49.*
                } else if (wn2.getNodeName().equalsIgnoreCase("randomizeOrigin")) { // Legacy, 0.49.7 Removal
                    retVal.getRandomOriginOptions().setRandomizeOrigin(Boolean.parseBoolean(wn2.getTextContent()));
                } else if (wn2.getNodeName().equalsIgnoreCase("randomizeDependentOrigin")) { // Legacy, 0.49.7 Removal
                    retVal.getRandomOriginOptions().setRandomizeDependentOrigin(Boolean.parseBoolean(wn2.getTextContent()));
                } else if (wn2.getNodeName().equalsIgnoreCase("originSearchRadius")) { // Legacy, 0.49.7 Removal
                    retVal.getRandomOriginOptions().setOriginSearchRadius(Integer.parseInt(wn2.getTextContent()));
                } else if (wn2.getNodeName().equalsIgnoreCase("extraRandomOrigin")) { // Legacy, 0.49.7 Removal
                    retVal.getRandomOriginOptions().setExtraRandomOrigin(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("originDistanceScale")) { // Legacy, 0.49.7 Removal
                    retVal.getRandomOriginOptions().setOriginDistanceScale(Double.parseDouble(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("retirementRolls")) { // Legacy - 0.49.7 Removal
                    final boolean value = Boolean.parseBoolean(wn2.getTextContent().trim());
                    retVal.setRandomRetirementMethod((value && retVal.getUseAtB()) ? RandomRetirementMethod.AGAINST_THE_BOT : RandomRetirementMethod.NONE);
                    retVal.setUseYearEndRandomRetirement(value);
                    retVal.setUseContractCompletionRandomRetirement(value);
                } else if (wn2.getNodeName().equalsIgnoreCase("customRetirementMods")) { // Legacy - 0.49.7 Removal
                    retVal.setUseCustomRetirementModifiers(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("foundersNeverRetire")) { // Legacy - 0.49.7 Removal
                    retVal.setUseRandomFounderRetirement(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("atbAddDependents")) { // Legacy - 0.49.7 Removal
                    final boolean value = Boolean.parseBoolean(wn2.getTextContent().trim());
                    retVal.setRandomDependentMethod((value && retVal.getUseAtB()) ? RandomDependentMethod.AGAINST_THE_BOT : RandomDependentMethod.NONE);
                    retVal.setUseRandomDependentAddition(value);
                } else if (wn2.getNodeName().equalsIgnoreCase("dependentsNeverLeave")) { // Legacy - 0.49.7 Removal
                    retVal.setUseRandomDependentRemoval(!Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("chanceRandomMarriages")) { // Legacy - 0.49.6 Removal
                    retVal.setPercentageRandomMarriageOppositeSexChance(Double.parseDouble(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("chanceRandomSameSexMarriages")) { // Legacy - 0.49.6 Removal
                    retVal.setPercentageRandomMarriageSameSexChance(Double.parseDouble(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("marriageAgeRange")) { // Legacy - 0.49.6 Removal
                    retVal.setRandomMarriageAgeRange(Integer.parseInt(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("useRandomMarriages")) { // Legacy - 0.49.6 Removal
                    retVal.setRandomMarriageMethod(Boolean.parseBoolean(wn2.getTextContent().trim())
                            ? RandomMarriageMethod.PERCENTAGE : RandomMarriageMethod.NONE);
                } else if (wn2.getNodeName().equalsIgnoreCase("logMarriageNameChange")) { // Legacy - 0.49.6 Removal
                    retVal.setLogMarriageNameChanges(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("randomMarriageSurnameWeights")) { // Legacy - 0.49.6 Removal
                    final String[] values = wn2.getTextContent().split(",");
                    if (values.length == 13) {
                        final MergingSurnameStyle[] marriageSurnameStyles = MergingSurnameStyle.values();
                        for (int i = 0; i < values.length; i++) {
                            retVal.getMarriageSurnameWeights().put(marriageSurnameStyles[i], Integer.parseInt(values[i]));
                        }
                    } else if (values.length == 9) {
                        retVal.migrateMarriageSurnameWeights47(values);
                    } else {
                        LogManager.getLogger().error("Unknown length of randomMarriageSurnameWeights");
                    }
                } else if (wn2.getNodeName().equalsIgnoreCase("useUnofficialProcreation") // Legacy - 0.49.0 Removal
                        || wn2.getNodeName().equalsIgnoreCase("useProcreation")) { // Legacy - 0.49.4 Removal
                    retVal.setRandomProcreationMethod(RandomProcreationMethod.PERCENTAGE);
                    retVal.setUseManualProcreation(true);
                } else if (wn2.getNodeName().equalsIgnoreCase("chanceProcreation")) { // Legacy - 0.49.4 Removal
                    retVal.setPercentageRandomProcreationRelationshipChance(Double.parseDouble(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("useUnofficialProcreationNoRelationship") // Legacy - 0.49.0 Removal
                        || wn2.getNodeName().equalsIgnoreCase("useProcreationNoRelationship")) { // Legacy - 0.49.4 Removal
                    retVal.setUseRelationshiplessRandomProcreation(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("chanceProcreationNoRelationship")) { // Legacy - 0.49.4 Removal
                    retVal.setPercentageRandomProcreationRelationshiplessChance(Double.parseDouble(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("logConception")) { // Legacy - 0.49.4 Removal
                    retVal.setLogProcreation(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("staticRATs")) { // Legacy - 0.49.4 Removal
                    retVal.setUseStaticRATs(true);
                } else if (wn2.getNodeName().equalsIgnoreCase("ignoreRatEra")) { // Legacy - 0.49.4 Removal
                    retVal.setIgnoreRATEra(true);
                } else if (wn2.getNodeName().equalsIgnoreCase("clanPriceModifier")) { // Legacy - 0.49.3 Removal
                    final double value = Double.parseDouble(wn2.getTextContent());
                    retVal.setClanUnitPriceMultiplier(value);
                    retVal.setClanPartPriceMultiplier(value);
                } else if (wn2.getNodeName().equalsIgnoreCase("usedPartsValueA")) { // Legacy - 0.49.3 Removal
                    retVal.getUsedPartPriceMultipliers()[0] = Double.parseDouble(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("usedPartsValueB")) { // Legacy - 0.49.3 Removal
                    retVal.getUsedPartPriceMultipliers()[1] = Double.parseDouble(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("usedPartsValueC")) { // Legacy - 0.49.3 Removal
                    retVal.getUsedPartPriceMultipliers()[2] = Double.parseDouble(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("usedPartsValueD")) { // Legacy - 0.49.3 Removal
                    retVal.getUsedPartPriceMultipliers()[3] = Double.parseDouble(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("usedPartsValueE")) { // Legacy - 0.49.3 Removal
                    retVal.getUsedPartPriceMultipliers()[4] = Double.parseDouble(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("usedPartsValueF")) { // Legacy - 0.49.3 Removal
                    retVal.getUsedPartPriceMultipliers()[5] = Double.parseDouble(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("damagedPartsValue")) { // Legacy - 0.49.3 Removal
                    retVal.setDamagedPartsValueMultiplier(Double.parseDouble(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("canceledOrderReimbursement")) { // Legacy - 0.49.3 Removal
                    retVal.setCancelledOrderRefundMultiplier(Double.parseDouble(wn2.getTextContent().trim()));

                // Removed in 0.47.*
                } else if (wn2.getNodeName().equalsIgnoreCase("useAtBCapture")) { // Legacy
                    if (Boolean.parseBoolean(wn2.getTextContent().trim())) {
                        retVal.setPrisonerCaptureStyle(PrisonerCaptureStyle.ATB);
                        retVal.setUseAtBPrisonerDefection(true);
                        retVal.setUseAtBPrisonerRansom(true);
                    }
                } else if (wn2.getNodeName().equalsIgnoreCase("intensity")) { // Legacy
                    double intensity = Double.parseDouble(wn2.getTextContent().trim());

                    retVal.atbBattleChance[AtBLanceRole.FIGHTING.ordinal()] = (int) Math.round(((40.0 * intensity) / (40.0 * intensity + 60.0)) * 100.0 + 0.5);
                    retVal.atbBattleChance[AtBLanceRole.DEFENCE.ordinal()] = (int) Math.round(((20.0 * intensity) / (20.0 * intensity + 80.0)) * 100.0 + 0.5);
                    retVal.atbBattleChance[AtBLanceRole.SCOUTING.ordinal()] = (int) Math.round(((60.0 * intensity) / (60.0 * intensity + 40.0)) * 100.0 + 0.5);
                    retVal.atbBattleChance[AtBLanceRole.TRAINING.ordinal()] = (int) Math.round(((10.0 * intensity) / (10.0 * intensity + 90.0)) * 100.0 + 0.5);
                } else if (wn2.getNodeName().equalsIgnoreCase("personnelMarketType")) { // Legacy
                    retVal.personnelMarketName = PersonnelMarket.getTypeName(Integer.parseInt(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("capturePrisoners")) { // Legacy
                    retVal.setPrisonerCaptureStyle(Boolean.parseBoolean(wn2.getTextContent().trim())
                            ? PrisonerCaptureStyle.TAHARQA : PrisonerCaptureStyle.NONE);
                } else if (wn2.getNodeName().equalsIgnoreCase("startGameDelay")) { // Legacy
                    MekHQ.getMHQOptions().setStartGameDelay(Integer.parseInt(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("historicalDailyLog")) { // Legacy
                    MekHQ.getMHQOptions().setHistoricalDailyLog(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("useUnitRating") // Legacy
                        || wn2.getNodeName().equalsIgnoreCase("useDragoonRating")) { // Legacy
                    if (!Boolean.parseBoolean(wn2.getTextContent())) {
                        retVal.setUnitRatingMethod(UnitRatingMethod.NONE);
                    }
                } else if (wn2.getNodeName().equalsIgnoreCase("probPhenoMW")) { // Legacy
                    retVal.phenotypeProbabilities[Phenotype.MECHWARRIOR.ordinal()] = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("probPhenoBA")) { // Legacy
                    retVal.phenotypeProbabilities[Phenotype.ELEMENTAL.ordinal()] = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("probPhenoAero")) { // Legacy
                    retVal.phenotypeProbabilities[Phenotype.AEROSPACE.ordinal()] = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("probPhenoVee")) { // Legacy
                    retVal.phenotypeProbabilities[Phenotype.VEHICLE.ordinal()] = Integer.parseInt(wn2.getTextContent().trim());
                }
                //endregion Legacy
            } catch (Exception e) {
                LogManager.getLogger().error("", e);
            }
        }

        // Fixing Old Data
        if (version.isLowerThan("0.49.3") && retVal.getUseAtB()) {
            retVal.setUnitMarketMethod(UnitMarketMethod.ATB_MONTHLY);
            retVal.setContractMarketMethod(ContractMarketMethod.ATB_MONTHLY);
        }

        LogManager.getLogger().debug("Load Campaign Options Complete!");

        return retVal;
    }

    /**
     * This is annoyingly required for the case of anyone having changed the surname weights.
     * The code is not nice, but will nicely handle the cases where anyone has made changes
     * @param values the values to migrate
     */
    public void migrateMarriageSurnameWeights47(final String... values) {
        int[] weights = new int[values.length];

        for (int i = 0; i < weights.length; i++) {
            try {
                weights[i] = Integer.parseInt(values[i]);
            } catch (Exception e) {
                LogManager.getLogger().error("", e);
                weights[i] = 0;
            }
        }

        // Now we need to test to figure out the weights have changed. If not, we will keep the
        // new default values. If they have, we save their changes and add the new surname weights
        if (
                (weights[0] != getMarriageSurnameWeights().get(MergingSurnameStyle.NO_CHANGE))
                        || (weights[1] != getMarriageSurnameWeights().get(MergingSurnameStyle.YOURS) + 5)
                        || (weights[2] != getMarriageSurnameWeights().get(MergingSurnameStyle.SPOUSE) + 5)
                        || (weights[3] != getMarriageSurnameWeights().get(MergingSurnameStyle.HYPHEN_SPOUSE) + 5)
                        || (weights[4] != getMarriageSurnameWeights().get(MergingSurnameStyle.BOTH_HYPHEN_SPOUSE) + 5)
                        || (weights[5] != getMarriageSurnameWeights().get(MergingSurnameStyle.HYPHEN_YOURS) + 5)
                        || (weights[6] != getMarriageSurnameWeights().get(MergingSurnameStyle.BOTH_HYPHEN_YOURS) + 5)
                        || (weights[7] != getMarriageSurnameWeights().get(MergingSurnameStyle.MALE))
                        || (weights[8] != getMarriageSurnameWeights().get(MergingSurnameStyle.FEMALE))
        ) {
            getMarriageSurnameWeights().put(MergingSurnameStyle.NO_CHANGE, weights[0]);
            getMarriageSurnameWeights().put(MergingSurnameStyle.YOURS, weights[1]);
            getMarriageSurnameWeights().put(MergingSurnameStyle.SPOUSE, weights[2]);
            // SPACE_YOURS is newly added
            // BOTH_SPACE_YOURS is newly added
            getMarriageSurnameWeights().put(MergingSurnameStyle.HYPHEN_YOURS, weights[3]);
            getMarriageSurnameWeights().put(MergingSurnameStyle.BOTH_HYPHEN_YOURS, weights[4]);
            // SPACE_SPOUSE is newly added
            // BOTH_SPACE_SPOUSE is newly added
            getMarriageSurnameWeights().put(MergingSurnameStyle.HYPHEN_SPOUSE, weights[5]);
            getMarriageSurnameWeights().put(MergingSurnameStyle.BOTH_HYPHEN_SPOUSE, weights[6]);
            getMarriageSurnameWeights().put(MergingSurnameStyle.MALE, weights[7]);
            getMarriageSurnameWeights().put(MergingSurnameStyle.FEMALE, weights[8]);
        }
    }
}
