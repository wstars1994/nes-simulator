//
// Created by WStars on 2021/9/9.
//

#include "usart.h"

UART_HandleTypeDef huart6;

/**
  * @brief  This function is executed in case of error occurrence.
  * @retval None
  */
void Error_Handler3(void) {
    /* USER CODE BEGIN Error_Handler_Debug */
    /* User can add his own implementation to report the HAL error return state */
    __disable_irq();
    while (1) {
    }
    /* USER CODE END Error_Handler_Debug */
}

/**
  * @brief USART6 Initialization Function
  * @param None
  * @retval None
  */
void MX_USART6_UART_Init(void) {

    huart6.Instance = USART6;
    huart6.Init.BaudRate = 921600;
    huart6.Init.WordLength = UART_WORDLENGTH_8B;
    huart6.Init.StopBits = UART_STOPBITS_1;
    huart6.Init.Parity = UART_PARITY_NONE;
    huart6.Init.Mode = UART_MODE_TX_RX;
    huart6.Init.HwFlowCtl = UART_HWCONTROL_NONE;
    huart6.Init.OverSampling = UART_OVERSAMPLING_16;
    if (HAL_UART_Init(&huart6) != HAL_OK) {
        Error_Handler3();
    }
}

void USART_Transmit(uint8_t data){
    HAL_UART_Transmit(&huart6,&data,1,0xffff);
}
