package com.sakura.companion.entity;

import com.sakura.companion.ModEntities;
import com.sakura.companion.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * Sakura: pendamping animasi.
 * Tahap hubungan: 0 = kenalan, 1 = pacaran, 2 = menikah.
 * Semua interaksi ramah semua umur.
 */
public class SakuraEntity extends TamableAnimal implements GeoEntity {
    public static final int STAGE_STRANGER = 0, STAGE_DATING = 1, STAGE_MARRIED = 2;
    private static final int DATING_AT = 30, MARRY_AT = 100, MAX_AFFECTION = 150;

    private static final EntityDataAccessor<Integer> STAGE =
            SynchedEntityData.defineId(SakuraEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> AFFECTION =
            SynchedEntityData.defineId(SakuraEntity.class, EntityDataSerializers.INT);
    /** 0 normal, 1 menari */
    private static final EntityDataAccessor<Integer> ACTION =
            SynchedEntityData.defineId(SakuraEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> WAVE_TICKS =
            SynchedEntityData.defineId(SakuraEntity.class, EntityDataSerializers.INT);

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation SIT = RawAnimation.begin().thenLoop("sit");
    private static final RawAnimation DANCE = RawAnimation.begin().thenLoop("dance");
    private static final RawAnimation WAVE = RawAnimation.begin().thenLoop("wave");
    private static final RawAnimation KISS = RawAnimation.begin().thenLoop("kiss");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private int kissTicks = 0;
    private int talkCooldown = 0;
    private int childCooldown = 0;

    public SakuraEntity(EntityType<? extends SakuraEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return TamableAnimal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 4.0)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(STAGE, STAGE_STRANGER);
        builder.define(AFFECTION, 0);
        builder.define(ACTION, 0);
        builder.define(WAVE_TICKS, 0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.2, true));
        goalSelector.addGoal(4, new FollowOwnerGoal(this, 1.1, 8.0f, 2.0f));
        goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.8));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0f));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        targetSelector.addGoal(3, new HurtByTargetGoal(this));
    }

    // ---------- data ----------
    public int getStage() { return entityData.get(STAGE); }
    public int getAffection() { return entityData.get(AFFECTION); }
    public boolean isDancing() { return entityData.get(ACTION) == 1; }
    public boolean isKissing() { return entityData.get(ACTION) == 2; }

    private void addAffection(int amount) {
        entityData.set(AFFECTION, Math.min(MAX_AFFECTION, getAffection() + amount));
    }

    private void say(Player player, String text) {
        player.displayClientMessage(Component.literal("§d[Sakura]§r " + text), false);
    }

    private void hearts(boolean happy) {
        if (level() instanceof ServerLevel sl) {
            sl.sendParticles(happy ? net.minecraft.core.particles.ParticleTypes.HEART
                            : net.minecraft.core.particles.ParticleTypes.SMOKE,
                    getX(), getY() + 1.9, getZ(), 6, 0.3, 0.3, 0.3, 0.02);
        }
    }

    // ---------- interaksi ----------
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level().isClientSide()) return InteractionResult.SUCCESS;

        boolean owner = isOwnedBy(player);

        // Cincin: melamar
        if (stack.is(ModItems.RING)) {
            if (getStage() == STAGE_DATING && owner) {
                if (getAffection() >= MARRY_AT) {
                    entityData.set(STAGE, STAGE_MARRIED);
                    stack.consume(1, player);
                    entityData.set(WAVE_TICKS, 80);
                    hearts(true);
                    say(player, "Iya, aku mau! Mulai sekarang kita menikah!");
                } else {
                    say(player, "Aku senang, tapi kita belum cukup dekat... (kedekatan "
                            + getAffection() + "/" + MARRY_AT + ")");
                }
            } else if (getStage() == STAGE_STRANGER) {
                say(player, "Eh, kita belum pacaran...");
            } else {
                say(player, "Kita sudah menikah, kok.");
            }
            return InteractionResult.SUCCESS;
        }

        // Pewarna pink: cium (pacaran atau menikah)
        if (stack.is(Items.PINK_DYE)) {
            if (getStage() >= STAGE_DATING && owner) {
                if (!isKissing()) {
                    entityData.set(ACTION, 2);
                    kissTicks = 50;
                    setOrderedToSit(false);
                    getNavigation().stop();
                    addAffection(3);
                    hearts(true);
                    say(player, getStage() == STAGE_MARRIED ? "Mmuah... sayangku." : "Mmuah! Kamu bikin aku malu...");
                }
            } else {
                hearts(false);
                say(player, "Eh! Kita belum sedekat itu...");
            }
            return InteractionResult.SUCCESS;
        }

        // Piringan musik: menari
        if (stack.has(DataComponents.JUKEBOX_PLAYABLE)) {
            boolean now = !isDancing();
            entityData.set(ACTION, now ? 1 : 0);
            if (now) { setOrderedToSit(false); addAffection(1); hearts(true); }
            return InteractionResult.SUCCESS;
        }

        // Kue: anak (hanya kalau sudah menikah)
        if (stack.is(Items.CAKE) || stack.is(Items.PUMPKIN_PIE)) {
            if (getStage() == STAGE_MARRIED && owner) {
                if (childCooldown <= 0 && level() instanceof ServerLevel sl) {
                    SakuraChildEntity child = ModEntities.CHILD.create(sl, net.minecraft.world.entity.EntitySpawnReason.BREEDING);
                    if (child != null) {
                        child.snapTo(getX(), getY(), getZ(), 0f, 0f);
                        child.tame(player);
                        sl.addFreshEntity(child);
                        stack.consume(1, player);
                        childCooldown = 20 * 60 * 10; // 10 menit
                        hearts(true);
                        say(player, "Kita makan kue bersama, ngobrol sampai larut... (layar memudar)");
                        say(player, "Beberapa waktu kemudian... anak kita lahir! Jaga dia baik-baik ya.");
                    }
                } else {
                    say(player, "Kita sudah punya anak. Tunggu beberapa saat lagi ya.");
                }
            } else {
                giveGift(player, stack, 3);
            }
            return InteractionResult.SUCCESS;
        }

        // Hadiah
        if (stack.is(Items.POPPY) || stack.is(Items.PINK_PETALS) || stack.is(Items.CHERRY_LEAVES)
                || stack.is(Items.SWEET_BERRIES) || stack.is(Items.COOKIE)
                || stack.is(Items.GOLDEN_CARROT) || stack.is(Items.DIAMOND)) {
            int value = stack.is(Items.DIAMOND) ? 10 : stack.is(Items.GOLDEN_CARROT) ? 5 : 3;
            giveGift(player, stack, value);
            return InteractionResult.SUCCESS;
        }

        // Tangan kosong
        if (stack.isEmpty()) {
            if (player.isShiftKeyDown() && owner) {
                setOrderedToSit(!isOrderedToSit());
                entityData.set(ACTION, 0);
                say(player, isOrderedToSit() ? "Oke, aku duduk dulu." : "Ayo jalan!");
            } else if (talkCooldown <= 0) {
                talk(player);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    private void giveGift(Player player, ItemStack stack, int value) {
        stack.consume(1, player);
        addAffection(value);
        entityData.set(WAVE_TICKS, 40);
        hearts(true);
        say(player, "Terima kasih! Aku suka banget.");
        checkDating(player);
    }

    private void talk(Player player) {
        talkCooldown = 20 * 20;
        addAffection(2);
        entityData.set(WAVE_TICKS, 40);
        hearts(true);
        String[] lines = switch (getStage()) {
            case STAGE_MARRIED -> new String[]{"Sayang, hari ini kita ngapain?", "Aku senang kita bersama.", "Ayo jalan-jalan lagi!"};
            case STAGE_DATING -> new String[]{"Kamu bikin hariku seru.", "Besok kita petualangan lagi, yuk?", "Aku nggak sabar ketemu kamu."};
            default -> new String[]{"Hai! Aku Sakura.", "Cuacanya enak ya.", "Kamu suka petualangan?"};
        };
        say(player, lines[random.nextInt(lines.length)]);
        checkDating(player);
    }

    private void checkDating(Player player) {
        if (getStage() == STAGE_STRANGER && getAffection() >= DATING_AT) {
            entityData.set(STAGE, STAGE_DATING);
            tame(player);
            setOrderedToSit(false);
            say(player, "Kita... pacaran sekarang, ya? Aku akan ikut denganmu!");
        }
    }

    // ---------- tick ----------
    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide()) {
            if (talkCooldown > 0) talkCooldown--;
            if (childCooldown > 0) childCooldown--;
            int w = entityData.get(WAVE_TICKS);
            if (w > 0) entityData.set(WAVE_TICKS, w - 1);
            if (isDancing() && (isOrderedToSit() || getTarget() != null)) entityData.set(ACTION, 0);
            if (isKissing()) {
                getNavigation().stop();
                if (--kissTicks <= 0) entityData.set(ACTION, 0);
                else if (kissTicks % 10 == 0) hearts(true);
            }
        }
    }

    // ---------- simpan ----------
    @Override
    public void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("Stage", getStage());
        output.putInt("Affection", getAffection());
    }

    @Override
    public void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        entityData.set(STAGE, input.getIntOr("Stage", STAGE_STRANGER));
        entityData.set(AFFECTION, input.getIntOr("Affection", 0));
    }

    @Override
    public boolean isFood(net.minecraft.world.item.ItemStack stack) { return false; }

    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) { return null; }

    // ---------- animasi GeckoLib ----------
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>("main", 4, state -> {
            if (isOrderedToSit()) return state.setAndContinue(SIT);
            if (isKissing()) return state.setAndContinue(KISS);
            if (isDancing()) return state.setAndContinue(DANCE);
            if (entityData.get(WAVE_TICKS) > 0) return state.setAndContinue(WAVE);
            if (state.isMoving()) return state.setAndContinue(WALK);
            return state.setAndContinue(IDLE);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }
}
