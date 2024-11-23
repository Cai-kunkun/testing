package dev.tr7zw.exordium.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import dev.tr7zw.exordium.ExordiumModBase;
import dev.tr7zw.exordium.access.BossOverlayAccess;
import dev.tr7zw.exordium.access.ChatAccess;
import dev.tr7zw.exordium.access.GuiAccess;
import dev.tr7zw.exordium.access.TablistAccess;
import dev.tr7zw.exordium.components.BufferInstance;
import dev.tr7zw.exordium.components.vanilla.BossHealthBarComponent;
import dev.tr7zw.exordium.components.vanilla.PlayerListComponent;
import dev.tr7zw.exordium.components.vanilla.PlayerListComponent.PlayerListContext;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;

//spotless:off
//#if MC >= 12100
import net.minecraft.client.DeltaTracker;
//#endif
//spotless:on

@Mixin(Gui.class)
public abstract class GuiMixin implements GuiAccess {

    @Shadow
    private ChatComponent chat;
    @Shadow
    private PlayerTabOverlay tabList;
    @Shadow
    protected int tickCount;

    @Shadow
    public abstract BossHealthOverlay getBossOverlay();

    @WrapOperation(method = "renderChat", at = {
            @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent;render(Lnet/minecraft/client/gui/GuiGraphics;IIIZ)V"), })
    private void renderChatWrapper(ChatComponent instance, GuiGraphics guiGraphics, int tickCount, int j, int k,
            boolean b, final Operation<Void> operation) {
        ChatAccess chatAccess = (ChatAccess) chat;
        BufferInstance<ChatAccess> buffer = ExordiumModBase.instance.getBufferManager()
                .getBufferInstance(dev.tr7zw.exordium.components.vanilla.ChatComponent.getId(), ChatAccess.class);
        if (!buffer.renderBuffer(tickCount, chatAccess)) {
            operation.call(instance, guiGraphics, tickCount, j, k, b);
        }
        buffer.postRender(chatAccess);
    }

    @WrapOperation(method = "renderTabList", at = {
            @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/PlayerTabOverlay;render(Lnet/minecraft/client/gui/GuiGraphics;ILnet/minecraft/world/scores/Scoreboard;Lnet/minecraft/world/scores/Objective;)V"), })
    private void renderTablistWrapper(PlayerTabOverlay instance, GuiGraphics guiGraphics, int screenWidth,
            Scoreboard scoreboard, Objective objective2, final Operation<Void> operation) {
        TablistAccess tablistAccess = (TablistAccess) tabList;
        BufferInstance<PlayerListContext> buffer = ExordiumModBase.instance.getBufferManager()
                .getBufferInstance(PlayerListComponent.getId(), PlayerListContext.class);
        PlayerListContext context = new PlayerListContext(tablistAccess, scoreboard, objective2);
        if (!buffer.renderBuffer(tickCount, context)) {
            operation.call(instance, guiGraphics, screenWidth, scoreboard, objective2);
        }
        buffer.postRender(context);
    }

    @WrapOperation(method = "method_55808", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/BossHealthOverlay;render(Lnet/minecraft/client/gui/GuiGraphics;)V"))
    private void renderBossBarWrapper(BossHealthOverlay instance, GuiGraphics guiGraphics, Operation<Void> original) {
        BossOverlayAccess overlayAccess = (BossOverlayAccess) this.getBossOverlay();
        @SuppressWarnings("unchecked")
        BufferInstance<BossOverlayAccess> buffer = ExordiumModBase.instance.getBufferManager()
                .getBufferInstance(BossHealthBarComponent.getId(), BossOverlayAccess.class);
        if (!buffer.renderBuffer(tickCount, overlayAccess)) {
            original.call(instance, guiGraphics);
        }
        buffer.postRender(overlayAccess);
    }

    @Inject(method = "render", at = @At(value = "TAIL"))
    // spotless:off
    //#if MC >= 12100
    public void render(GuiGraphics guiGraphics, DeltaTracker partialTick, CallbackInfo ci) {
    //#else
    //$$ public void render(GuiGraphics guiGraphics, float partialTick, CallbackInfo ci) {
    //#endif
    //spotless:on
        ExordiumModBase.instance.getDelayedRenderCallManager().renderComponents();
    }

    @Override
    public ChatComponent getChatComponent() {
        return chat;
    }

    @Override
    public PlayerTabOverlay getPlayerTabOverlay() {
        return tabList;
    }

    @Override
    public int getTickCount() {
        return tickCount;
    }

}
