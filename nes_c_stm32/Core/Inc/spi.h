//
// Created by Administrator on 2021/7/10.
//

#ifndef STM32_SDCARD_SPI_H
#define STM32_SDCARD_SPI_H

#endif //STM32_SDCARD_SPI_H

#include "stm32f4xx_hal.h"

void MX_SPI1_Init(void);
void MX_SPI2_Init(void);

void MX_SPI1_Baud(uint32_t baud_rate);

void Spi_Write_Data(uint8_t data);
uint8_t Spi_Read_Data(void);
void Spi2_Write_Data(uint8_t data);
void Spi2_Write_Data_16(uint16_t data);