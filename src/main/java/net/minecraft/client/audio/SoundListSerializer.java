package net.minecraft.client.audio;

import com.google.gson.*;
import net.minecraft.util.JsonUtils;
import org.apache.commons.lang3.Validate;

import java.lang.reflect.Type;
import java.util.Objects;

public class SoundListSerializer implements JsonDeserializer<SoundList> {
    public SoundList deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext ctx) throws JsonParseException {
        JsonObject jsonobject = JsonUtils.getJsonObject(jsonElement, "entry");
        SoundList soundlist = new SoundList();
        soundlist.setReplaceExisting(JsonUtils.getBoolean(jsonobject, "replace", false));
        SoundCategory soundcategory = SoundCategory.getCategory(JsonUtils.getString(jsonobject, "category", SoundCategory.MASTER.getCategoryName()));
        soundlist.setSoundCategory(soundcategory);
        Objects.requireNonNull(soundcategory, "Invalid category");

        if (jsonobject.has("sounds")) {
            JsonArray jsonarray = JsonUtils.getJsonArray(jsonobject, "sounds");

            for (int i = 0; i < jsonarray.size(); ++i) {
                JsonElement jsonelement = jsonarray.get(i);
                SoundList.SoundEntry soundlist$soundentry = new SoundList.SoundEntry();

                if (JsonUtils.isString(jsonelement)) {
                    soundlist$soundentry.setSoundEntryName(JsonUtils.getString(jsonelement, "sound"));
                } else {
                    JsonObject jsonobject1 = JsonUtils.getJsonObject(jsonelement, "sound");
                    soundlist$soundentry.setSoundEntryName(JsonUtils.getString(jsonobject1, "name"));

                    if (jsonobject1.has("type")) {
                        SoundList.SoundEntry.Type soundlist$soundentry$type = SoundList.SoundEntry.Type.getType(JsonUtils.getString(jsonobject1, "type"));
                        Objects.requireNonNull(soundlist$soundentry$type, "Invalid type");
                        soundlist$soundentry.setSoundEntryType(soundlist$soundentry$type);
                    }

                    if (jsonobject1.has("volume")) {
                        float f = JsonUtils.getFloat(jsonobject1, "volume");
                        Validate.isTrue(f > 0.0F, "Invalid volume");
                        soundlist$soundentry.setSoundEntryVolume(f);
                    }

                    if (jsonobject1.has("pitch")) {
                        float f1 = JsonUtils.getFloat(jsonobject1, "pitch");
                        Validate.isTrue(f1 > 0.0F, "Invalid pitch");
                        soundlist$soundentry.setSoundEntryPitch(f1);
                    }

                    if (jsonobject1.has("weight")) {
                        int j = JsonUtils.getInt(jsonobject1, "weight");
                        Validate.isTrue(j > 0, "Invalid weight");
                        soundlist$soundentry.setSoundEntryWeight(j);
                    }

                    if (jsonobject1.has("stream")) {
                        soundlist$soundentry.setStreaming(JsonUtils.getBoolean(jsonobject1, "stream"));
                    }
                }

                soundlist.getSoundList().add(soundlist$soundentry);
            }
        }

        return soundlist;
    }
}
