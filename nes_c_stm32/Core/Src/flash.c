//
// Created by WStars on 2022/5/1.
//

#include "flash.h"

ErrorStatus LL_FLASH_Unlock(){
  if (READ_BIT(FLASH->CR, FLASH_CR_LOCK) != RESET) {
    /* Authorize the FLASH Registers access */
    WRITE_REG(FLASH->KEYR, 0x45670123);
    WRITE_REG(FLASH->KEYR, 0xCDEF89AB);
  }
  return READ_BIT(FLASH->CR, FLASH_CR_LOCK);
}

void LL_FLASH_Erase_Sector(uint32_t Sector){
  CLEAR_BIT(FLASH->CR, FLASH_CR_PSIZE);
  FLASH->CR |= FLASH_PSIZE_WORD;
  CLEAR_BIT(FLASH->CR, FLASH_CR_SNB);
  FLASH->CR |= FLASH_CR_SER | (Sector << FLASH_CR_SNB_Pos);
  FLASH->CR |= FLASH_CR_STRT;
}
void LL_FLASH_Program_Byte(uint32_t Address,uint8_t Data){
  CLEAR_BIT(FLASH->CR, FLASH_CR_PSIZE);
  FLASH->CR |= FLASH_PSIZE_BYTE;
  FLASH->CR |= FLASH_CR_PG;
  *(__IO uint8_t*)Address = Data;
}