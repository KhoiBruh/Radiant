package net.minecraft.client.network;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.exceptions.InvalidCredentialsException;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.NetworkState;
import net.minecraft.network.login.INetHandlerLoginClient;
import net.minecraft.network.login.client.C01PacketEncryptionResponse;
import net.minecraft.network.login.server.S00PacketDisconnect;
import net.minecraft.network.login.server.S01PacketEncryptionRequest;
import net.minecraft.network.login.server.S02PacketLoginSuccess;
import net.minecraft.network.login.server.S03PacketEnableCompression;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.CryptManager;
import net.minecraft.util.IChatComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.SecretKey;
import java.math.BigInteger;
import java.security.PublicKey;

public class NetHandlerLoginClient implements INetHandlerLoginClient {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Minecraft mc;
    private final GuiScreen previousGuiScreen;
    private final NetworkManager networkManager;
    private GameProfile gameProfile;

    public NetHandlerLoginClient(NetworkManager networkManagerIn, Minecraft mcIn, GuiScreen p_i45059_3_) {
        this.networkManager = networkManagerIn;
        this.mc = mcIn;
        this.previousGuiScreen = p_i45059_3_;
    }

    public void handleEncryptionRequest(S01PacketEncryptionRequest packet) {
        final SecretKey secretkey = CryptManager.createNewSharedKey();
        String s = packet.getServerId();
        PublicKey publickey = packet.getPublicKey();
        String s1 = (new BigInteger(CryptManager.getServerIdHash(s, publickey, secretkey))).toString(16);

        if (this.mc.getCurrentServerData() != null && this.mc.getCurrentServerData().isOnLAN()) {
            try {
                this.getSessionService().joinServer(this.mc.getSession().getProfile(), this.mc.getSession().getToken(), s1);
            } catch (AuthenticationException exception) {
                LOGGER.warn("Couldn't connect to auth servers but will continue to join LAN");
            }
        } else {
            try {
                this.getSessionService().joinServer(this.mc.getSession().getProfile(), this.mc.getSession().getToken(), s1);
            } catch (AuthenticationUnavailableException exception) {
                this.networkManager.closeChannel(new ChatComponentTranslation("disconnect.loginFailedInfo", new ChatComponentTranslation("disconnect.loginFailedInfo.serversUnavailable")));
                return;
            } catch (InvalidCredentialsException exception) {
                this.networkManager.closeChannel(new ChatComponentTranslation("disconnect.loginFailedInfo", new ChatComponentTranslation("disconnect.loginFailedInfo.invalidSession")));
                return;
            } catch (AuthenticationException exception) {
                this.networkManager.closeChannel(new ChatComponentTranslation("disconnect.loginFailedInfo", exception.getMessage()));
                return;
            }
        }

        this.networkManager.sendPacket(new C01PacketEncryptionResponse(secretkey, publickey, packet.getVerifyToken()), p_operationComplete_1_ -> NetHandlerLoginClient.this.networkManager.enableEncryption(secretkey));
    }

    private MinecraftSessionService getSessionService() {
        return this.mc.getSessionService();
    }

    public void handleLoginSuccess(S02PacketLoginSuccess packet) {
        this.gameProfile = packet.getProfile();
        this.networkManager.setConnectionState(NetworkState.PLAY);
        this.networkManager.setNetHandler(new NetHandlerPlayClient(this.mc, this.previousGuiScreen, this.networkManager, this.gameProfile));
    }

    public void onDisconnect(IChatComponent reason) {
        this.mc.displayGuiScreen(new GuiDisconnected(this.previousGuiScreen, "connect.failed", reason));
    }

    public void handleDisconnect(S00PacketDisconnect packet) {
        this.networkManager.closeChannel(packet.getReason());
    }

    public void handleEnableCompression(S03PacketEnableCompression packet) {
        if (!this.networkManager.isLocalChannel()) {
            this.networkManager.setCompressionThreshold(packet.getCompressionThreshold());
        }
    }
}
