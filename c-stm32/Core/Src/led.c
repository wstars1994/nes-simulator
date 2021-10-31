//
// Created by WStars on 2021/8/29.
//
#define LCD_GPIO_TYPE  GPIOC
#define LCD_PIN        GPIO_PIN_13

#include "led.h"

void led_open(void) {

    HAL_GPIO_WritePin(LCD_GPIO_TYPE, LCD_PIN, GPIO_PIN_RESET);
}

void led_close(void) {

    HAL_GPIO_WritePin(LCD_GPIO_TYPE, LCD_PIN, GPIO_PIN_SET);
}