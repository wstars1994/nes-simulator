//
// Created by Administrator on 2021/7/10.
//

#include <malloc.h>
#include "sdcard.h"

void flash(uint8_t times,uint8_t intervalMs)
{
    for (int i = 0; i < times; ++i) {
        HAL_Delay(intervalMs);
        HAL_GPIO_WritePin(GPIOC, GPIO_PIN_13, GPIO_PIN_RESET);
        HAL_Delay(intervalMs);
        HAL_GPIO_WritePin(GPIOC, GPIO_PIN_13, GPIO_PIN_SET);
        HAL_Delay(intervalMs);
    }
    HAL_Delay(300);
}

void SD_Init(void){
    uint8_t i;
    //写至少74个clock
    for(i=0;i<10;i++) Spi_Write_Data(0xFF);
    //cmd0进入SPI模式
    SD_Send_Cmd(GO_IDLE_STATE,0,0x95,IDLE_STATE);
    flash(1,100);
//    cmd8检查
    SD_Send_Cmd8();
    flash(2,100);
//    cmd55切换为app模式然后发送cmd41告诉SD卡主机支持大容量SD卡操作,成功后sd卡退出IDLE_STATE进入INACTIVE_STATE
    SD_Send_Cmd41();
    flash(3,100);
    MX_SPI1_Baud(SPI_BAUDRATEPRESCALER_4);
}

/**
  * @brief send a command to sd
  * @param cmd
  * @param arg
  * @param crc
  * @retval None
  */
void SD_Write_Cmd(uint8_t cmd,uint32_t arg,uint8_t crc)
{
    Spi_Write_Data(0x40|cmd);
    Spi_Write_Data((arg>>24)&0xff);
    Spi_Write_Data((arg>>16)&0xff);
    Spi_Write_Data((arg>>8)&0xff);
    Spi_Write_Data(arg&0xff);
    Spi_Write_Data(crc);
    Spi_Write_Data(0xff);
}

void SD_Send_Cmd(uint8_t cmd,uint32_t arg,uint8_t crc,uint8_t success_flag){
    uint8_t ret;
    do{
        SD_CS_L;
        SD_Write_Cmd(cmd,arg,crc);
        ret = Spi_Read_Data();
        SD_CS_H;
    } while (ret!=success_flag);
    //空脉冲
    Spi_Write_Data(0xFF);
}

void SD_Send_Cmd8(void)
{
    uint8_t ret;
    //支持2.7V - 3.6V
    uint8_t voltage_sup;
    do{
        SD_CS_L;
        SD_Write_Cmd(SEND_IF_COND,0x1AA,0x87);
        ret = Spi_Read_Data();
        if(ret == IDLE_STATE){
            Spi_Read_Data();
            Spi_Read_Data();
            voltage_sup = Spi_Read_Data();
            ret = Spi_Read_Data();
        }
        SD_CS_H;
    } while (voltage_sup!=1&&ret!=0xAA);
    Spi_Write_Data(0xFF);
}

void SD_Send_Cmd41(void)
{
    uint8_t ret;
    do{
        //cmd55切换为app模式然后发送cmd41初始化SD卡
        SD_CS_L;
        SD_Write_Cmd(APP_CMD,0,0x65);
        ret = Spi_Read_Data();
        Spi_Write_Data(0xFF);
        if(ret == IDLE_STATE){
            SD_Write_Cmd(SD_SEND_OP_COND,0x40000000,0xE3);
            ret = Spi_Read_Data();
        }
        SD_CS_H;
    } while (ret);
    Spi_Write_Data(0xFF);
}

void SD_Send_Cmd58(void)
{
    uint8_t ret;
//    do{
        SD_CS_L;
        SD_Write_Cmd(READ_OCR,0x0,0x69);
        ret = Spi_Read_Data();
        if(ret == IDLE_STATE || ret == ZERO_STATE){
            Spi_Read_Data();
            Spi_Read_Data();
            Spi_Read_Data();
            ret = Spi_Read_Data();
        }
        SD_CS_H;
//    } while (ret!=0xAA);
    Spi_Write_Data(0xFF);
}


void SD_Send_Cmd9(void)
{
    uint8_t ret,i,csd[16];
    SD_CS_L;
    SD_Write_Cmd(SEND_CSD,0,0xAF);
    ret = Spi_Read_Data();
    if(ret == ZERO_STATE){
        ret = Spi_Read_Data();
        if(ret == 0xFE){
            for (i = 0; i < 16; i++)
            {
                csd[i] = Spi_Read_Data();
            }
        }
    }
    //crc
    Spi_Write_Data(0xFF);
    Spi_Write_Data(0xFF);
    SD_CS_H;
    Spi_Write_Data(0xFF);
}

uint8_t * SD_Send_Cmd17(int block)
{
    uint8_t *res = NULL;
    res = (uint8_t*)malloc(sizeof(uint8_t)*512);
    uint8_t ret;
    SD_CS_L;
    //第8192个块=0x40000
    SD_Write_Cmd(READ_SINGLE_BLOCK,8192+block,0x01);
    ret = Spi_Read_Data();
    if(ret == ZERO_STATE){
        do {
            ret = Spi_Read_Data();
        } while (ret!=0xFE);
        for (int i = 0; i < 512; i++)
        {
            res[i] = Spi_Read_Data();
        }
    }
    SD_CS_H;
    Spi_Write_Data(0xFF);
    Spi_Write_Data(0xFF);
    Spi_Write_Data(0xFF);
    Spi_Write_Data(0xFF);
    return res;
}