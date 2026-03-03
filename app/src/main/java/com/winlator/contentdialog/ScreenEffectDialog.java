package com.winlator.contentdialog;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.preference.PreferenceManager;

import app.gamenative.R;
import com.winlator.core.AppUtils;
import com.winlator.core.KeyValueSet;
import com.winlator.renderer.GLRenderer;
import com.winlator.renderer.effects.ColorEffect;
import com.winlator.renderer.effects.CRTEffect;
import com.winlator.renderer.effects.FXAAEffect;
import com.winlator.renderer.effects.NTSCCombinedEffect;
import com.winlator.renderer.effects.ToonEffect;
import com.winlator.widget.SeekBar;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

public class ScreenEffectDialog extends ContentDialog {

    private final GLRenderer renderer;
    private final CheckBox cbEnableCRTShader;
    private final CheckBox cbEnableFXAA;
    private final CheckBox cbEnableToonShader;
    private final CheckBox cbEnableNTSCEffect;
    private final SharedPreferences preferences;
    private final Spinner sProfile;
    private final SeekBar sbBrightness;
    private final SeekBar sbContrast;
    private final SeekBar sbGamma;
    private String screenEffectProfile;

    private static final String TAG = "ScreenEffectDialog";


    public ScreenEffectDialog(Context context, GLRenderer renderer) {
        super(context, R.layout.screen_effect_dialog);
        this.renderer = renderer;

        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.screenEffectProfile = preferences.getString("last_screen_effect_profile", "");

        sProfile = findViewById(R.id.SProfile);
        sbBrightness = findViewById(R.id.SBBrightness);
        sbContrast = findViewById(R.id.SBContrast);
        sbGamma = findViewById(R.id.SBGamma);
        cbEnableFXAA = findViewById(R.id.CBEnableFXAA);
        cbEnableCRTShader = findViewById(R.id.CBEnableCRTShader);

        cbEnableToonShader = findViewById(R.id.CBEnableToonShader);
        cbEnableNTSCEffect = findViewById(R.id.CBEnableNTSCEffect);

        ColorEffect colorEffect = (ColorEffect) renderer.getEffectComposer().getEffect(ColorEffect.class);
        FXAAEffect fxaaEffect = (FXAAEffect) renderer.getEffectComposer().getEffect(FXAAEffect.class);
        CRTEffect crtEffect = (CRTEffect) renderer.getEffectComposer().getEffect(CRTEffect.class);
        ToonEffect toonEffect = (ToonEffect) renderer.getEffectComposer().getEffect(ToonEffect.class);
        NTSCCombinedEffect ntscEffect = (NTSCCombinedEffect) renderer.getEffectComposer().getEffect(NTSCCombinedEffect.class);

        if (colorEffect != null) {
            sbBrightness.setValue(colorEffect.getBrightness() * 100);
            sbContrast.setValue(colorEffect.getContrast() * 100);
            sbGamma.setValue(colorEffect.getGamma());
        } else {
            resetSettings();
        }

        cbEnableFXAA.setChecked(fxaaEffect != null);
        cbEnableCRTShader.setChecked(crtEffect != null);
        cbEnableToonShader.setChecked(toonEffect != null);
        cbEnableNTSCEffect.setChecked(ntscEffect != null);

        loadProfileSpinner(sProfile, screenEffectProfile);

        sProfile.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    loadProfile(sProfile.getSelectedItem().toString());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        Button resetButton = findViewById(R.id.BTReset);
        resetButton.setVisibility(View.VISIBLE);
        resetButton.setOnClickListener(v -> resetSettings());

        findViewById(R.id.BTConfirm).setOnClickListener(v -> {
            saveProfile(sProfile);
            applyEffects(colorEffect, renderer, fxaaEffect, crtEffect, toonEffect, ntscEffect);
            dismiss();
        });

        findViewById(R.id.BTAddProfile).setOnClickListener(v -> promptAddProfile());
        findViewById(R.id.BTRemoveProfile).setOnClickListener(v -> promptDeleteProfile());

        setOnConfirmCallback(() -> {
            applyEffects(colorEffect, renderer, fxaaEffect, crtEffect, toonEffect, ntscEffect);
            dismiss();
        });

        // Apply white text to all sub-views for consistent theming
        View root = getContentView();
        if (root instanceof ViewGroup) setTextColorForDialog((ViewGroup) root, 0xFFFFFFFF);
    }

    private void promptAddProfile() {
        ContentDialog.prompt(getContext(), R.string.do_you_want_to_add_a_new_profile, null, name -> addProfile(name, sProfile));
    }

    private void promptDeleteProfile() {
        if (sProfile.getSelectedItemPosition() > 0) {
            String selectedProfile = sProfile.getSelectedItem().toString();
            ContentDialog.confirm(getContext(), R.string.do_you_want_to_remove_this_profile, () -> removeProfile(selectedProfile, sProfile));
        } else {
            AppUtils.showToast(getContext(), R.string.no_profile_selected);
        }
    }

    private void addProfile(String newName, Spinner sProfile) {
        Set<String> profiles = new LinkedHashSet<>(preferences.getStringSet("screen_effect_profiles", new LinkedHashSet<>()));
        for (String profile : profiles) {
            String[] parts = profile.split(":");
            if (parts[0].equals(newName)) {
                return;
            }
        }
        profiles.add(newName + ":");
        preferences.edit().putStringSet("screen_effect_profiles", profiles).apply();
        loadProfileSpinner(sProfile, newName);
    }

    private void loadProfileSpinner(Spinner sProfile, String selectedName) {
        Set<String> profiles = new LinkedHashSet<>(preferences.getStringSet("screen_effect_profiles", new LinkedHashSet<>()));
        ArrayList<String> items = new ArrayList<>();
        items.add("-- " + getContext().getString(R.string.default_profile) + " --");
        int selectedPosition = 0, position = 1;
        for (String profile : profiles) {
            String[] parts = profile.split(":");
            items.add(parts[0]);
            if (parts[0].equals(selectedName)) {
                selectedPosition = position;
            }
            position++;
        }
        sProfile.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, items));
        sProfile.setSelection(selectedPosition);
    }

    private void loadProfile(String name) {
        Set<String> profiles = new LinkedHashSet<>(preferences.getStringSet("screen_effect_profiles", new LinkedHashSet<>()));
        for (String profile : profiles) {
            String[] parts = profile.split(":");
            if (parts[0].equals(name) && parts.length > 1 && !parts[1].isEmpty()) {
                KeyValueSet settings = new KeyValueSet(parts[1]);
                sbBrightness.setValue(settings.getFloat("brightness", 0));
                sbContrast.setValue(settings.getFloat("contrast", 1.0f));
                sbGamma.setValue(settings.getFloat("gamma", 1.0f));
                cbEnableFXAA.setChecked(settings.getBoolean("fxaa", false));
                cbEnableCRTShader.setChecked(settings.getBoolean("crt_shader", false));
                cbEnableToonShader.setChecked(settings.getBoolean("toon_shader", false));
                cbEnableNTSCEffect.setChecked(settings.getBoolean("ntsc_effect", false));
                return;
            }
        }
    }

    private void removeProfile(String targetName, Spinner sProfile) {
        Set<String> profiles = new LinkedHashSet<>(preferences.getStringSet("screen_effect_profiles", new LinkedHashSet<>()));
        profiles.removeIf(profile -> profile.split(":")[0].equals(targetName));
        preferences.edit().putStringSet("screen_effect_profiles", profiles).apply();
        loadProfileSpinner(sProfile, null);
        resetSettings();
    }

    private void resetSettings() {
        sbBrightness.setValue(0);
        sbContrast.setValue(0);
        sbGamma.setValue(1.0f);
        cbEnableFXAA.setChecked(false);
        cbEnableCRTShader.setChecked(false);
        cbEnableToonShader.setChecked(false);
        cbEnableNTSCEffect.setChecked(false);
    }

    private void saveProfile(Spinner sProfile) {
        if (sProfile.getSelectedItemPosition() > 0) {
            String selectedProfile = sProfile.getSelectedItem().toString();
            Set<String> oldProfiles = new LinkedHashSet<>(preferences.getStringSet("screen_effect_profiles", new LinkedHashSet<>()));
            Set<String> newProfiles = new LinkedHashSet<>();
            KeyValueSet settings = new KeyValueSet();
            settings.put("brightness", sbBrightness.getValue());
            settings.put("contrast", sbContrast.getValue());
            settings.put("gamma", sbGamma.getValue());
            settings.put("fxaa", cbEnableFXAA.isChecked());
            settings.put("crt_shader", cbEnableCRTShader.isChecked());
            settings.put("toon_shader", cbEnableToonShader.isChecked());
            settings.put("ntsc_effect", cbEnableNTSCEffect.isChecked());

            for (String profile : oldProfiles) {
                String[] parts = profile.split(":");
                if (parts[0].equals(selectedProfile)) {
                    newProfiles.add(selectedProfile + ":" + settings.toString());
                } else {
                    newProfiles.add(profile);
                }
            }
            preferences.edit().putStringSet("screen_effect_profiles", newProfiles).apply();
            preferences.edit().putString("last_screen_effect_profile", selectedProfile).apply();
            this.screenEffectProfile = selectedProfile;
        }
    }

    public void applyEffects(ColorEffect colorEffect, GLRenderer renderer, FXAAEffect fxaaEffect, CRTEffect crtEffect, ToonEffect toonEffect, NTSCCombinedEffect ntscEffect) {
        float brightness = sbBrightness.getValue();
        float contrast = sbContrast.getValue();
        float gamma = sbGamma.getValue();
        boolean enableFXAA = cbEnableFXAA.isChecked();
        boolean enableCRTShader = cbEnableCRTShader.isChecked();
        boolean enableToonShader = cbEnableToonShader.isChecked();
        boolean enableNTSCEffect = cbEnableNTSCEffect.isChecked();

        if (colorEffect == null) colorEffect = new ColorEffect();

        if (renderer == null || renderer.getEffectComposer() == null) return;

        if (brightness == 0 && contrast == 0 && gamma == 1.0f) {
            renderer.getEffectComposer().removeEffect(colorEffect);
        } else {
            colorEffect.setBrightness(brightness / 100f);
            colorEffect.setContrast(contrast / 100f);
            colorEffect.setGamma(gamma);
            renderer.getEffectComposer().addEffect(colorEffect);
        }

        if (enableFXAA) {
            if (fxaaEffect == null) {
                fxaaEffect = new FXAAEffect();
                renderer.getEffectComposer().addEffect(fxaaEffect);
            }
        } else if (fxaaEffect != null) renderer.getEffectComposer().removeEffect(fxaaEffect);

        if (enableCRTShader) {
            if (crtEffect == null) {
                crtEffect = new CRTEffect();
                renderer.getEffectComposer().addEffect(crtEffect);
            }
        } else if (crtEffect != null) renderer.getEffectComposer().removeEffect(crtEffect);

        if (enableToonShader) {
            if (toonEffect == null) {
                toonEffect = new ToonEffect();
                renderer.getEffectComposer().addEffect(toonEffect);
            }
        } else if (toonEffect != null) renderer.getEffectComposer().removeEffect(toonEffect);

        if (enableNTSCEffect) {
            if (ntscEffect == null) {
                ntscEffect = new NTSCCombinedEffect();
                renderer.getEffectComposer().addEffect(ntscEffect);
            }
        } else if (ntscEffect != null) renderer.getEffectComposer().removeEffect(ntscEffect);

        saveProfile(sProfile);
    }

    private void setTextColorForDialog(ViewGroup viewGroup, int color) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            if (child instanceof ViewGroup) {
                setTextColorForDialog((ViewGroup) child, color);
            } else if (child instanceof TextView || child instanceof CheckBox || child instanceof Button) {
                ((TextView) child).setTextColor(color);
            }
        }
    }
}
