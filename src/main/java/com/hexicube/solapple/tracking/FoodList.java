package com.hexicube.solapple.tracking;

import com.hexicube.solapple.SOLAppleConfig;
import com.hexicube.solapple.api.FoodCapability;
import com.hexicube.solapple.api.SOLAppleAPI;
import net.minecraft.core.Direction;
import net.minecraft.nbt.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

@ParametersAreNonnullByDefault
public final class FoodList implements FoodCapability {
	private static final String NBT_KEY_FOOD_LIST = "foodList";
	
	public static FoodList get(Player player) {
		return (FoodList) player.getCapability(SOLAppleAPI.foodCapability)
			.orElseThrow(FoodListNotFoundException::new);
	}
	
	private final Set<FoodInstance> foods = new HashSet<>();
	
	@Nullable
	private ProgressInfo cachedProgressInfo;
	
	public FoodList() {}
	
	private final LazyOptional<FoodList> capabilityOptional = LazyOptional.of(() -> this);
	
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
		return capability == SOLAppleAPI.foodCapability ? capabilityOptional.cast() : LazyOptional.empty();
	}
	
	/** used for persistent storage */
	@Override
	public CompoundTag serializeNBT() {
		var tag = new CompoundTag();
		
		var list = new ListTag();
		foods.stream()
			.map(FoodInstance::encode)
			.filter(Objects::nonNull)
			.map(StringTag::valueOf)
			.forEach(list::add);
		tag.put(NBT_KEY_FOOD_LIST, list);
		
		return tag;
	}
	
	/** used for persistent storage */
	@Override
	public void deserializeNBT(CompoundTag tag) {
		var list = tag.getList(NBT_KEY_FOOD_LIST, Tag.TAG_STRING);
		
		foods.clear();
		list.stream()
			.map(nbt -> (StringTag) nbt)
			.map(StringTag::getAsString)
			.map(FoodInstance::decode)
			.filter(Objects::nonNull)
			.forEach(foods::add);
		
		invalidateProgressInfo();
	}
	
	/** @return true if the food was not previously known, i.e. if a new food has been tried */
	public boolean addFood(Item food) {
		boolean wasAdded = foods.add(new FoodInstance(food)) && SOLAppleConfig.isAllowed(food);
		invalidateProgressInfo();
		return wasAdded;
	}
	
	@Override
	public boolean hasEaten(Item food) {
		if (!food.isEdible()) return false;
		return foods.contains(new FoodInstance(food));
	}
	
	public void clearFood() {
		foods.clear();
		invalidateProgressInfo();
	}
	
	public Set<FoodInstance> getEatenFoods() {
		return new HashSet<>(foods);
	}
	
	// TODO: is this actually desirable? it doesn't filter at all
	@Override
	public int getEatenFoodCount() {
		return foods.size();
	}
	
	public ProgressInfo getProgressInfo() {
		if (cachedProgressInfo == null) {
			cachedProgressInfo = new ProgressInfo(this, SOLAppleConfig.getFoodGroups());
		}
		return cachedProgressInfo;
	}
	
	public void invalidateProgressInfo() {
		cachedProgressInfo = null;
	}
	
	public static class FoodListNotFoundException extends RuntimeException {
		public FoodListNotFoundException() {
			super("Player must have food capability attached, but none was found.");
		}
	}
}
