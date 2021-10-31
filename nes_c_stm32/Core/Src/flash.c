//
// Created by WStars on 2021/9/10.
//
#include <nes.h>
#include "flash.h"
#include "sdcard.h"

HAL_StatusTypeDef Flash_Write() {
	//解锁flash
	HAL_FLASH_Unlock();

	//定义擦除结构体
	FLASH_EraseInitTypeDef flash;
	//只擦除sector
	flash.TypeErase = FLASH_TYPEERASE_SECTORS;
	//擦除sector
	flash.Sector = FLASH_SECTOR_5;
	//擦除一个
	flash.NbSectors = 1;

	flash.VoltageRange = FLASH_VOLTAGE_RANGE_3;

    uint32_t errorPage = 0;
    HAL_StatusTypeDef resStatus = HAL_FLASHEx_Erase(&flash, &errorPage);

    if(resStatus == HAL_OK) {
        for (int i = 0; i < 10513-10432; ++i) {
            byte *res = SD_Send_Cmd17(10432+i);
            for (int j = 0; j < 512; ++j) {
                HAL_FLASH_Program(FLASH_TYPEPROGRAM_BYTE,0x08020000+i*512+j,res[j]);
            }
        }
    }
	HAL_FLASH_Lock();
    return HAL_OK;
}

