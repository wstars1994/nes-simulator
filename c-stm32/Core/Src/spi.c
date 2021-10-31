//
// Created by Administrator on 2021/7/10.
//

#include "spi.h"
DMA_HandleTypeDef hdma_spi2_tx;
SPI_HandleTypeDef hspi1;
SPI_HandleTypeDef hspi2;

/**
  * @brief  This function is executed in case of error occurrence.
  * @retval None
  */
void Error_Handler2(void) {
    /* USER CODE BEGIN Error_Handler_Debug */
    /* User can add his own implementation to report the HAL error return state */
    __disable_irq();
    while (1) {
    }
    /* USER CODE END Error_Handler_Debug */
}

void MX_SPI1_Init(void) {
    /* SPI1 parameter configuration*/
    hspi1.Instance = SPI1;
    hspi1.Init.Mode = SPI_MODE_MASTER;
    hspi1.Init.Direction = SPI_DIRECTION_2LINES;
    hspi1.Init.DataSize = SPI_DATASIZE_8BIT;
    hspi1.Init.CLKPolarity = SPI_POLARITY_LOW;
    hspi1.Init.CLKPhase = SPI_PHASE_1EDGE;
    hspi1.Init.NSS = SPI_NSS_SOFT;
    hspi1.Init.BaudRatePrescaler = SPI_BAUDRATEPRESCALER_256;
    hspi1.Init.FirstBit = SPI_FIRSTBIT_MSB;
    hspi1.Init.TIMode = SPI_TIMODE_DISABLE;
    hspi1.Init.CRCCalculation = SPI_CRCCALCULATION_DISABLE;
    hspi1.Init.CRCPolynomial = 10;
    if (HAL_SPI_Init(&hspi1) != HAL_OK) {
        Error_Handler2();
    }
}

void MX_SPI1_Baud(uint32_t baud_rate) {
    hspi1.Init.BaudRatePrescaler = baud_rate;
}

/**
  * @brief  向sd发送一个字节
  * @retval None
  */
void Spi_Write_Data(uint8_t data)
{
    HAL_SPI_Transmit(&hspi1,&data,1, 0xffff);
}

/**
  * @brief  从sd接收一个字节
  * @retval
  */
uint8_t Spi_Read_Data()
{
    uint8_t rxData;
    uint8_t txData = 0xFF;
    HAL_SPI_TransmitReceive(&hspi1,&txData,&rxData,1, 0xffff);
    return rxData;
}


/**
  * @brief  向sd发送一个字节
  * @retval None
  */
void Spi2_Write_Data(uint8_t data)
{
    HAL_SPI_Transmit(&hspi2,&data,1,0xffff);
}

void Spi2_Write_Data_16(uint16_t data)
{
    uint16_t high = ((data&0xff)<<8) | (data>>8&0xff);
    HAL_SPI_Transmit(&hspi2,(uint8_t*)&high,2,0xffff);
}



/**
  * @brief SPI2 Initialization Function
  * @param None
  * @retval None
  */
void MX_SPI2_Init(void)
{
    /* SPI2 parameter configuration*/
    hspi2.Instance = SPI2;
    hspi2.Init.Mode = SPI_MODE_MASTER;
    hspi2.Init.Direction = SPI_DIRECTION_2LINES;
    hspi2.Init.DataSize = SPI_DATASIZE_8BIT;
    hspi2.Init.CLKPolarity = SPI_POLARITY_LOW;
    hspi2.Init.CLKPhase = SPI_PHASE_1EDGE;
    hspi2.Init.NSS = SPI_NSS_SOFT;
    hspi2.Init.BaudRatePrescaler = SPI_BAUDRATEPRESCALER_2;
    hspi2.Init.FirstBit = SPI_FIRSTBIT_MSB;
    hspi2.Init.TIMode = SPI_TIMODE_DISABLE;
    hspi2.Init.CRCCalculation = SPI_CRCCALCULATION_DISABLE;
    hspi2.Init.CRCPolynomial = 10;
    if (HAL_SPI_Init(&hspi2) != HAL_OK)
    {
        Error_Handler2();
    }
}