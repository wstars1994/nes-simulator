#include "stm32f4xx_hal.h"
#include "spi.h"

#define SD_CS_H HAL_GPIO_WritePin(GPIOA, GPIO_PIN_4, GPIO_PIN_SET);
#define SD_CS_L HAL_GPIO_WritePin(GPIOA, GPIO_PIN_4, GPIO_PIN_RESET);

/**
  * SD Card 命令
  */
typedef enum
{
    GO_IDLE_STATE = 0,
    SEND_IF_COND = 8,
    APP_CMD = 55,
    READ_OCR = 58,
    SD_SEND_OP_COND = 41,
    SEND_CSD = 9,
    READ_SINGLE_BLOCK = 17
} SD_CMD;

/**
  * SD Card 状态
  */
typedef enum
{
    ZERO_STATE = 0,
    IDLE_STATE = 0x01

} SD_STATUS;

void SD_Init(void);

void SD_Send_Cmd(uint8_t cmd,uint32_t arg,uint8_t crc,uint8_t success_flag);

void SD_Send_Cmd8(void);

void SD_Send_Cmd9(void);

void SD_Send_Cmd58(void);

void SD_Send_Cmd41(void);

uint8_t* SD_Send_Cmd17(int block);

