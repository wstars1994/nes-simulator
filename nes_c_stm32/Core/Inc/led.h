//
// Created by WStars on 2021/12/5.
//

#ifndef SPITEST_LED_H
#define SPITEST_LED_H

#endif //SPITEST_LED_H

#define LED_ON LL_GPIO_ResetOutputPin(GPIOC, LL_GPIO_PIN_13)
#define LED_OFF LL_GPIO_SetOutputPin(GPIOC, LL_GPIO_PIN_13);