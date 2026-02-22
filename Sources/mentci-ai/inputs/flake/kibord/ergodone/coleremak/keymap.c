#include QMK_KEYBOARD_H
#include "version.h"
/* #include "bootmagic.h" */

#define _______ KC_TRNS

#define COLEMAK 0   // colemak layer
#define QWERTY 1    // qwerty layer
#define FN 2        // function layer

// leader key timeout
#define LEADER_TIMEOUT 350
// reset timer after each key
#define LEADER_PER_KEY_TIMING

enum custom_keycodes {
    QWRTY = SAFE_RANGE, // can always be here
    CLMK,
    VRSN,
    FNCTN
};

/* false: Caps Lock LED is off
   true: Caps Lock LED is on */
bool CAPS_LED = false;

const uint16_t PROGMEM keymaps[][MATRIX_ROWS][MATRIX_COLS] = {
[QWERTY] = LAYOUT_ergodox(
            // left hand
            KC_1,                KC_2,           KC_3,     KC_4,             KC_5,          KC_COPY,       KC_GRV, 
            KC_Q,                KC_W,           KC_E,     KC_R,             KC_T,          KC_MUTE,       KC_TAB, 
            KC_Z,                LCTL_T(KC_S),   LALT_T(KC_D),     KC_F,             KC_G,          CTL_T(KC_ESC),  
            MT(MOD_LSFT, KC_A),  KC_X,           KC_C,     KC_V,             LGUI_T(KC_B),  KC_FN1,        KC_LSPO, 
            KC_LCTL,             KC_LEFT,        KC_RGHT,  LT(FN, KC_BSPC),  KC_LEAD,        
                                                                                               KC_CAPS,  KC_LGUI,
                                                                                                         KC_HOME,
                                                                                      KC_SPC,  KC_BSPC,  KC_END,
            // right hand
                       KC_PASTE,  KC_BSPC,   KC_6,          KC_7,           KC_8,     KC_9,          KC_0,                    
                       KC_LBRC,   KC_BSLS,   KC_Y,          KC_U,           KC_I,     KC_O,          KC_P,                    
	               KC_QUOTE,  KC_H,          KC_J,      RALT_T(KC_K),     RCTL_T(KC_L),  KC_SLSH,         
	               KC_RBRC,   KC_FN1,    RGUI_T(KC_N),  KC_M,           KC_COMM,  KC_DOT,        MT(MOD_RSFT,KC_SCLN),
	                                     LALT(KC_SPC),   LT(FN,KC_ESC),  KC_DOWN,  KC_UP,         KC_EQL,
             KC_INS,    KC_DEL,
             KC_PGUP,
             KC_PGDN,   KC_TAB,  KC_ENT
),

[COLEMAK] = LAYOUT_ergodox(
            // left hand
            KC_1,                KC_2,           KC_3,     KC_4,             KC_5,          KC_COPY,       KC_GRV, 
            KC_Q,                KC_W,           KC_F,     KC_P,             KC_G,          KC_MUTE,       KC_TAB, 
            KC_Z,                LCTL_T(KC_R),   LALT_T(KC_S),  KC_T,        KC_D,          CTL_T(KC_ESC),  
            MT(MOD_LSFT, KC_A),  KC_X,           KC_C,     KC_V,             LGUI_T(KC_B),  KC_FN1,        KC_LSPO, 

            KC_LCTL,             KC_LEFT,        KC_RGHT,  LT(FN, KC_BSPC),  KC_LEAD,        
                                                                                               KC_CAPS,  KC_LGUI,
                                                                                                         KC_HOME,
                                                                                      KC_SPC,  KC_BSPC,  KC_END,
            // right hand
                       KC_PASTE,  KC_BSPC,   KC_6,          KC_7,           KC_8,     KC_9,          KC_0,
	    KC_LBRC,   KC_BSLS,   KC_J,          KC_L,           KC_U,     KC_Y,          KC_SCLN,
                                  KC_QUOTE,  KC_H,          KC_N,           RALT_T(KC_E),     RCTL_T(KC_I),  KC_SLSH,
	               KC_RBRC,   KC_FN1,    RGUI_T(KC_K),  KC_M,           KC_COMM,  KC_DOT,        MT(MOD_RSFT,KC_O),
	                                     LALT(KC_SPC),   LT(FN,KC_ESC),  KC_DOWN,  KC_UP,         KC_EQL,
             KC_INS,    KC_DEL,
             KC_PGUP,
             KC_PGDN,   KC_TAB,  KC_ENT
),

[FN] = LAYOUT_ergodox(
       // left hand
       _______,       _______,    KC_MPRV,   KC_MPLY,    KC_MNXT,       _______,    VRSN,
       KC_GRV,        KC_EXLM,    KC_AT,     KC_HASH,    KC_DLR,        _______,    KC_F11,
       KC_TAB ,       LCTL_T(KC_1), KC_2,      KC_3,       KC_MINS,       KC_MINS,
       LSFT_T(KC_0),  KC_LPRN,    KC_LBRC,   KC_LCBR,    LGUI_T(KC_4),  KC_FN1,      _______,
       KC_LCTL,       KC_LEFT,    KC_RGHT,   LT(FN, KC_BSPC),  KC_LEAD,        
                                                                          KC_CAPS,  KC_LGUI,
                                                                                    KC_HOME,
                                                                 KC_SPC,  KC_BSPC,  KC_END,
       // right hand
       _______,  RESET,   CLMK,            QWRTY,      _______,     _______,    _______, 
       _______,  KC_F12,  KC_CIRC,         KC_AMPR,    KC_ASTR,     KC_PERC,    KC_BSLS, 
                 KC_EQL,  KC_EQL,          KC_6,       KC_7,        RCTL_T(KC_8), KC_QUOT, 
       _______,  KC_FN1,  RGUI_T(KC_5),    KC_RCBR,    KC_RBRC,     KC_RPRN,    RSFT_T(KC_9),
                          LALT(KC_SPC),   LT(FN,KC_ESC),  KC_DOWN,  KC_UP,         KC_EQL,
       KC_INS,    KC_DEL,
       KC_PGUP,
       KC_PGDN,  KC_TAB,  KC_ENT
),
};

const uint16_t PROGMEM fn_actions[] = {
    [1] = ACTION_LAYER_TAP_TOGGLE(FN)                // FN1 - Momentary Layer 1
};

bool process_record_user(uint16_t keycode, keyrecord_t *record) {
    switch (keycode) {
      case VRSN:
        if (record->event.pressed) {
          SEND_STRING (QMK_KEYBOARD "/" QMK_KEYMAP " @ " QMK_VERSION);
        }
        return false;
        break;
      case QWRTY:
        if (record->event.pressed) {
          set_single_persistent_default_layer(QWERTY);
        }
        return false;
        break;
      case CLMK:
        if (record->event.pressed) {
          set_single_persistent_default_layer(COLEMAK);
        }
        return false;
        break;
      case KC_CAPS:
        if (record->event.pressed) {
          // Turn LED1 On/Off for Caps Lock
          if (CAPS_LED) {
            ergodox_right_led_1_off();
            CAPS_LED = false;
          } else {
            ergodox_right_led_1_on();
            CAPS_LED = true;
          }
        }
        return true;
        break;
    }
    return true;
};

// Set LED according to the default layer
void default_layer_led_set(void) {
    switch (biton32(eeconfig_read_default_layer())) {
      case COLEMAK:
        // LED2 for COLEMAK
        ergodox_right_led_2_on();
        break;
      case QWERTY:
        // LED3 for QWERTY
        ergodox_right_led_3_on();
        break;
    };
};

// Runs just one time when the keyboard initializes.
void matrix_init_user(void) {
    default_layer_led_set();
};

uint32_t layer_state_set_user(uint32_t state) {
    ergodox_led_all_off();
    switch (biton32(state)) {
      case FN:
          // Red led on Pro Micro for Fn layer
          ergodox_board_led_on();
          break;
    };

    if (CAPS_LED) {
      ergodox_right_led_1_on();
    }

    default_layer_led_set();

    return state;
};

void suspend_power_down_user(void) {
    ergodox_led_all_off();
};

void suspend_wakeup_init_user(void) {
    default_layer_led_set();
};

#define TAP_ONCE(code)  \
  register_code (code); \
  unregister_code (code)

LEADER_EXTERNS();

void matrix_scan_user(void) {
  LEADER_DICTIONARY() {
    leading = false;
    leader_end();

    SEQ_ONE_KEY(RALT(KC_SPC)) {
        TAP_ONCE(KC_FN1);
    }
    SEQ_ONE_KEY(KC_A) {
        TAP_ONCE(KC_ESC);
    }
    SEQ_ONE_KEY(KC_R) {
        TAP_ONCE(KC_TAB);
    }
    SEQ_ONE_KEY(KC_I) {
        register_code(KC_LSFT);
        register_code(KC_TAB);
        unregister_code(KC_TAB);
        unregister_code(KC_LSFT);
    }
    SEQ_ONE_KEY(KC_RALT) {
        register_code(KC_RALT);
        register_code(KC_SPC);
        unregister_code(KC_SPC);
        unregister_code(KC_RALT);
    }
    SEQ_ONE_KEY(KC_N) {
        register_code(KC_RALT);
        register_code(KC_SPC);
        unregister_code(KC_SPC);
        unregister_code(KC_RALT);
    }
    SEQ_ONE_KEY(KC_O) {
      TAP_ONCE(KC_ESC);
    }
    SEQ_ONE_KEY(KC_I) {
        layer_invert(2);
    }
    SEQ_ONE_KEY(KC_ESC) {
        layer_invert(2);
    }
    SEQ_ONE_KEY(KC_BSPC) {
        layer_invert(2);
    }
  }
}
