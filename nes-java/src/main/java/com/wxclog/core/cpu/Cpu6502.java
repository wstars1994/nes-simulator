package com.wxclog.core.cpu;

import com.wxclog.util.LogUtil;

import java.util.Iterator;

/**
 * @description: 模拟6502cpu
 * @author: WStars
 * @date: 2020-04-18 20:12
 */
public class Cpu6502{

    /**
     * 1.79 = CPU频率约1.79MHz
     * 60 = NTSC制式 262条扫描线
     * 计算出每条扫描数所需要的CPU周期
     * 1.79*Math.pow(10,6)/60/262 ≈ 113
     */
    public static int cpuCycle = 113;

    private static int runCycleNum = 0;

    /**
     * CPU内存数据
     */
    private CpuMemory cpuMemory = new CpuMemory();
    /**
     * CPU寄存器
     */
    private CpuRegister cpuRegister;


    public CpuMemory getCpuMemory() {
        return cpuMemory;
    }

    public CpuRegister getCpuRegister() {
        return cpuRegister;
    }

    public Cpu6502(byte[] prgData){
        cpuMemory.write(0x8000,prgData);
        cpuRegister = new CpuRegister(cpuMemory);
    }
    /**
     * 运行程序
     */
    public void go(){
        cpuCycle = 113;
        Iterator<Byte> iterator = cpuMemory.iteratorPrgData();
        do{
            LogUtil.logf("\n");
            int prgPc = cpuMemory.getPrgPc();
            byte insCode = iterator.next();
//            LogUtil.logf("PC:[%06d] | CYC:[%03d] | PC:[%04X] | OPC:[%02X] | R:[A:%02X X:%02X Y:%02X S:%02X] | F:[N:%d V:%d B:%d D:%d I:%d Z:%d C:%d]",
            LogUtil.logf("PC:[%d] | CYC:[%d] | PC:[%X] | OPC:[%X] | R:[A:%X X:%X Y:%X S:%X] | F:[N:%d V:%d B:%d D:%d I:%d Z:%d C:%d]",
                    ++runCycleNum,
                    cpuCycle,
                    prgPc&0xFFFF,
                    insCode&0xFF,
                    cpuRegister.getReg_A()&0xFF,cpuRegister.getReg_X()&0xFF,cpuRegister.getReg_Y()&0xFF,cpuRegister.getReg_S()&0xFF,
                    cpuRegister.getN(),cpuRegister.getV(),cpuRegister.getB(),cpuRegister.getD(),cpuRegister.getI(),cpuRegister.getZ(),cpuRegister.getC());
            this.execInstrcution(insCode,iterator);
        }while (cpuCycle > 0);
    }

    /**
     * 执行指令
     * @param insCode
     */
    private void execInstrcution(byte insCode,Iterator<Byte> iterator) {
        //执行程序(超级玛丽的执行顺序)
        switch(insCode&0xff) {
            //SEI 禁止中断
            case 0x78:
				cpuCycle-= cpuRegister.F_I((byte) 1);
                break;
            //CLD 清除十进制模式状态标记
            case 0xD8:
				cpuCycle-= cpuRegister.CLD();
                break;
            //LDA 将数据存入累加器A
            case 0xA9:
				cpuCycle-= cpuRegister.LDA(iterator.next());
                break;
            //STA 将累加器A的数据存入CPU内存
            case 0x8D:
				cpuCycle-= cpuRegister.STA_ABS(iterator.next(),iterator.next());
                break;
            //LDX 将数据存入X索引寄存器
            case 0xA2:
				cpuCycle-= cpuRegister.LDX(iterator.next());
                break;
            //TXS 将X索引寄存器的数据存入栈指针SP
            case 0x9A:
				cpuCycle-= cpuRegister.TXS();
                break;
            //LDA Absolute
            case 0xAD:
				cpuCycle-= cpuRegister.LDA_ABS(iterator.next(),iterator.next());
                break;
            //BPL
            case 0x10:
				cpuCycle-= cpuRegister.BPL(iterator.next());
                break;
            //LDY 将数据存入Y索引寄存器
            case 0xA0:
				cpuCycle-= cpuRegister.LDY(iterator.next());
                break;
            //LDA Absolute X
            case 0xBD:
				cpuCycle-= cpuRegister.LDA_ABS_X(iterator.next(),iterator.next());
                break;
            //CMP
            case 0xC9:
				cpuCycle-= cpuRegister.CMP(iterator.next());
                break;
            //BCS
            case 0xB0:
				cpuCycle-= cpuRegister.BCS(iterator.next());
                break;
            //DEX
            case 0xCA:
				cpuCycle-= cpuRegister.DEX();
                break;
            //BNE
            case 0xD0:
				cpuCycle-= cpuRegister.BNE(iterator.next());
                break;
            //JSR
            case 0x20:
				cpuCycle-= cpuRegister.JSR(iterator.next(),iterator.next());
                break;
            //STA 将累加器A的数据存入CPU内存
            case 0x85:
				cpuCycle-= cpuRegister.STA(iterator.next());
                break;
            //STX 将寄存器X的数据存入CPU内存
            case 0x86:
				cpuCycle-= cpuRegister.STX_ZERO(iterator.next());
                break;
            //CPX
            case 0xE0:
				cpuCycle-= cpuRegister.CPX(iterator.next());
                break;
            //STA_INDIRECT_Y
            case 0x91:
				cpuCycle-= cpuRegister.STA_INDIRECT_Y(iterator.next());
                break;
            //DEY
            case 0x88:
				cpuCycle-= cpuRegister.DEY();
                break;
            //CPY
            case 0xC0:
				cpuCycle-= cpuRegister.CPY(iterator.next());
                break;
            //RTS
            case 0x60:
				cpuCycle-= cpuRegister.RTS();
                break;
            //BRK
            case 0x00:
				cpuCycle-= cpuRegister.BRK();
                break;
            //SED
            case 0xF8:
				cpuCycle-= cpuRegister.SED();
                break;
            //BIT
            case 0x2C:
				cpuCycle-= cpuRegister.BIT_ABS(iterator.next(),iterator.next());
                break;
            //BIT
            case 0x99:
				cpuCycle-= cpuRegister.STA_ABS_Y(iterator.next(),iterator.next());
                break;
            //INY
            case 0xC8:
				cpuCycle-= cpuRegister.INY();
                break;
            //ORA
            case 0x09:
				cpuCycle-= cpuRegister.ORA(iterator.next());
                break;
            //AND
            case 0x29:
				cpuCycle-= cpuRegister.AND(iterator.next());
                break;
            //TXA
            case 0x8A:
				cpuCycle-= cpuRegister.TXA();
                break;
            //JMP_ABS
            case 0x4C:
				cpuCycle-= cpuRegister.JMP_ABS(iterator.next(),iterator.next());
                break;
            //INC_ABS
            case 0xEE:
				cpuCycle-= cpuRegister.INC_ABS(iterator.next(),iterator.next());
                break;
            //LDY_ABS
            case 0xAC:
				cpuCycle-= cpuRegister.LDY_ABS(iterator.next(),iterator.next());
                break;
            //LDX_ABS
            case 0xAE:
				cpuCycle-= cpuRegister.LDX_ABS(iterator.next(),iterator.next());
                break;
            //LDA_INDIRECT_Y
            case 0xB1:
				cpuCycle-= cpuRegister.LDA_INDIRECT_Y(iterator.next());
                break;
            //LDX_ABS_Y
            case 0xBE:
				cpuCycle-= cpuRegister.LDX_ABS_Y(iterator.next(),iterator.next());
                break;
            //STA_ABS_X
            case 0x9D:
				cpuCycle-= cpuRegister.STA_ABS_X(iterator.next(),iterator.next());
                break;
            //LSR
            case 0x4A:
				cpuCycle-= cpuRegister.LSR();
                break;
            //TAX
            case 0xAA:
				cpuCycle-= cpuRegister.TAX();
                break;
            //PHA
            case 0x48:
				cpuCycle-= cpuRegister.PHA();
                break;
            //ORA_ZERO
            case 0x05:
				cpuCycle-= cpuRegister.ORA_ZERO(iterator.next());
                break;
            //PLA
            case 0x68:
				cpuCycle-= cpuRegister.PLA();
                break;
            //ROL
            case 0x2A:
				cpuCycle-= cpuRegister.ROL_A();
                break;
            //AND_ABS_X
            case 0x3D:
				cpuCycle-= cpuRegister.AND_ABS_X(iterator.next(),iterator.next());
                break;
            //BEQ
            case 0xF0:
				cpuCycle-= cpuRegister.BEQ(iterator.next());
                break;
            //INX
            case 0xE8:
				cpuCycle-= cpuRegister.INX();
                break;
            //SEC
            case 0x38:
				cpuCycle-= cpuRegister.SEC();
                break;
            //SBC
            case 0xF9:
				cpuCycle-= cpuRegister.SBC_ABS_Y(iterator.next(),iterator.next());
                break;
            //BCC
            case 0x90:
				cpuCycle-= cpuRegister.BCC(iterator.next());
                break;
            //EOR_ZERO
            case 0x45:
				cpuCycle-= cpuRegister.EOR_ZERO(iterator.next());
                break;
            //CLC
            case 0x18:
				cpuCycle-= cpuRegister.CLC();
                break;
            //ROR_ABS_X
            case 0x7E:
				cpuCycle-= cpuRegister.ROR_ABS_X(iterator.next(),iterator.next());
                break;
            //RTI
            case 0x40:
				cpuCycle-= cpuRegister.RTI();
                break;
            //DEC_ABS
            case 0xCE:
				cpuCycle-= cpuRegister.DEC_ABS(iterator.next(),iterator.next());
                break;
            //INC_ZERO
            case 0xE6:
				cpuCycle-= cpuRegister.INC_ZERO(iterator.next());
                break;
            //ASL_A
            case 0x0A:
				cpuCycle-= cpuRegister.ASL_A();
                break;
            //TAY
            case 0xA8:
				cpuCycle-= cpuRegister.TAY();
                break;
            //JMP_INDIRECT
            case 0x6C:
				cpuCycle-= cpuRegister.JMP_INDIRECT(iterator.next(),iterator.next());
                break;
            //LDA_ABS_Y
            case 0xB9:
				cpuCycle-= cpuRegister.LDA_ABS_Y(iterator.next(),iterator.next());
                break;
            //ADC_ABS
            case 0x6D:
				cpuCycle-= cpuRegister.ADC_ABS(iterator.next(),iterator.next());
                break;
            //ADC
            case 0x69:
				cpuCycle-= cpuRegister.ADC(iterator.next());
                break;
            //STY_ABS
            case 0x8C:
				cpuCycle-= cpuRegister.STY_ABS(iterator.next(),iterator.next());
                break;
            //LDA_ZERO
            case 0xA5:
				cpuCycle-= cpuRegister.LDA_ZERO(iterator.next());
                break;
            //DEC_ZERO
            case 0xC6:
				cpuCycle-= cpuRegister.DEC_ZERO(iterator.next());
                break;
            //TYA
            case 0x98:
				cpuCycle-= cpuRegister.TYA();
                break;
            //ADC_ZERO
            case 0x65:
				cpuCycle-= cpuRegister.ADC_ZERO(iterator.next());
                break;
            //LDX_ZERO
            case 0xA6:
				cpuCycle-= cpuRegister.LDX_ZERO(iterator.next());
                break;
            //STX_ABS
            case 0x8E:
				cpuCycle-= cpuRegister.STX_ABS(iterator.next(),iterator.next());
                break;
            //BMI
            case 0x30:
				cpuCycle-= cpuRegister.BMI(iterator.next());
                break;
            //ADC_ABS_Y
            case 0x79:
				cpuCycle-= cpuRegister.ADC_ABS_Y(iterator.next(),iterator.next());
                break;
            //SBC
            case 0xE9:
				cpuCycle-= cpuRegister.SBC(iterator.next());
                break;
            //STY_ZERO
            case 0x84:
				cpuCycle-= cpuRegister.STY_ZERO(iterator.next());
                break;
            //BIT_ZERO
            case 0x24:
				cpuCycle-= cpuRegister.BIT_ZERO(iterator.next());
                break;
            //LDY_ZERO
            case 0xA4:
				cpuCycle-= cpuRegister.LDY_ZERO(iterator.next());
                break;
            //CMP_ABS
            case 0xCD:
				cpuCycle-= cpuRegister.CMP_ABS(iterator.next(),iterator.next());
                break;
            //CMP_ABS_Y
            case 0xD9:
				cpuCycle-= cpuRegister.CMP_ABS_Y(iterator.next(),iterator.next());
                break;
            //EOR
            case 0x49:
				cpuCycle-= cpuRegister.EOR_A(iterator.next());
                break;
            //ROL_ZERO
            case 0x26:
				cpuCycle-= cpuRegister.ROL_ZERO(iterator.next()&0xff);
                break;
            //LSR_ZERO
            case 0x46:
				cpuCycle-= cpuRegister.LSR_ZERO(iterator.next()&0xff);
                break;
            //DEC_ABS_X
            case 0xDE:
				cpuCycle-= cpuRegister.DEC_ABS_X(iterator.next(),iterator.next());
                break;
            //LSR_ABS
            case 0x4E:
				cpuCycle-= cpuRegister.LSR_ABS(iterator.next(),iterator.next());
                break;
            //ROR_A
            case 0x6A:
				cpuCycle-= cpuRegister.ROR_A();
                break;
            //ROL_ABS
            case 0x2E:
				cpuCycle-= cpuRegister.ROL_ABS(iterator.next(),iterator.next());
                break;
            //CMP_ZERO
            case 0xC5:
				cpuCycle-= cpuRegister.CMP_ZERO(iterator.next());
                break;
            //LDY_ABS_X
            case 0xBC:
				cpuCycle-= cpuRegister.LDY_ABS_X(iterator.next(),iterator.next());
                break;
            //STA_ZERO_X
            case 0x95:
				cpuCycle-= cpuRegister.STA_ZERO_X(iterator.next());
                break;
            //CMP_ABS_X
            case 0xDD:
				cpuCycle-= cpuRegister.CMP_ABS_X(iterator.next(),iterator.next());
                break;
            //PHP
            case 0x08:
				cpuCycle-= cpuRegister.PHP();
                break;
            //CPX_ZERO
            case 0xE4:
				cpuCycle-= cpuRegister.CPX_ZERO(iterator.next());
                break;
            //ROR_ZERO
            case 0x66:
				cpuCycle-= cpuRegister.ROR_ZERO(iterator.next()&0xff);
                break;
            //LDA_ZERO_X
            case 0xB5:
				cpuCycle-= cpuRegister.LDA_ZERO_X(iterator.next());
                break;
            //AND_ZERO
            case 0x25:
				cpuCycle-= cpuRegister.AND_ZERO(iterator.next());
                break;
            //PLP
            case 0x28:
				cpuCycle-= cpuRegister.PLP();
                break;
            //ASL_ZERO
            case 0x06:
				cpuCycle-= cpuRegister.ASL_ZERO(iterator.next()&0xff);
                break;
            //AND_INDIRECT_Y
            case 0x31:
				cpuCycle-= cpuRegister.AND_INDIRECT_Y(iterator.next());
                break;
            //SBC_ZERO
            case 0xE5:
				cpuCycle-= cpuRegister.SBC_ZERO(iterator.next());
                break;
            //LDY_ZERO_X
            case 0xB4:
				cpuCycle-= cpuRegister.LDY_ZERO_X(iterator.next());
                break;
            //NOP
            case 0xEA:
				cpuCycle-= cpuRegister.NOP();
                break;
            //EOR_ABS
            case 0x4D:
				cpuCycle-= cpuRegister.EOR_ABS(iterator.next(),iterator.next());
                break;
            //TSX
            case 0xBA:
				cpuCycle-= cpuRegister.TSX();
                break;
            //ASL_ABS
            case 0x0E:
				cpuCycle-= cpuRegister.ASL_ABS(iterator.next(),iterator.next());
                break;
            //SBC_ABS
            case 0xED:
				cpuCycle-= cpuRegister.SBC_ABS(iterator.next(),iterator.next());
                break;
            //ORA_ABS
            case 0x0D:
				cpuCycle-= cpuRegister.ORA_ABS(iterator.next(),iterator.next());
                break;
            //CMP_ZERO_X
            case 0xD5:
				cpuCycle-= cpuRegister.CMP_ZERO_X(iterator.next());
                break;
            //SBC_ZERO_X
            case 0xF5:
				cpuCycle-= cpuRegister.SBC_ZERO_X(iterator.next());
                break;
            //AND_ABS_Y
            case 0x39:
				cpuCycle-= cpuRegister.AND_ABS_Y(iterator.next(),iterator.next());
                break;
//            //AND_ABS
            case 0x2D:
				cpuCycle-= cpuRegister.AND_ABS(iterator.next(),iterator.next());
                break;
            //ADC_ABS_X
            //TODO 可能有问题
            case 0x7D:
				cpuCycle-= cpuRegister.ADC_ABS_X(iterator.next(),iterator.next());
                break;
            //ADC_ZERO_X
            case 0x75:
				cpuCycle-= cpuRegister.ADC_ZERO_X(iterator.next());
                break;
            //DEC_ZERO_X
            case 0xD6:
				cpuCycle-= cpuRegister.DEC_ZERO_X(iterator.next());
                break;
            //开始调试马戏团
            //EOR
            case 0x55:
				cpuCycle-= cpuRegister.EOR_ZERO_X(iterator.next());
                break;
            //AND_ZERO_X
            case 0x35:
				cpuCycle-= cpuRegister.AND_ZERO_X(iterator.next());
                break;
            //STY_ZERO_X
            case 0x94:
				cpuCycle-= cpuRegister.STY_ZERO_X(iterator.next());
                break;
            //INC_ZERO_X
            case 0xF6:
				cpuCycle-= cpuRegister.INC_ZERO_X(iterator.next());
                break;
            //ORA_ZERO_X
            case 0x15:
				cpuCycle-= cpuRegister.ORA_ZERO_X(iterator.next());
                break;
            //BVC
            case 0x50:
				cpuCycle-= cpuRegister.BVC(iterator.next());
                break;
            //SBC_ABS_X
            case 0xFD:
				cpuCycle-= cpuRegister.SBC_ABS_X(iterator.next(),iterator.next());
                break;
            //CPY_ZERO
            case 0xC4:
				cpuCycle-= cpuRegister.CPY_ZERO(iterator.next());
                break;
            //ORA_INDIRECT_Y
            case 0x11:
				cpuCycle-= cpuRegister.ORA_INDIRECT_Y(iterator.next());
                break;
            //ORA_ABS_Y
            case 0x19:
				cpuCycle-= cpuRegister.ORA_ABS_Y(iterator.next(),iterator.next());
                break;
            //ADC_INDIRECT_Y
            case 0x71:
				cpuCycle-= cpuRegister.ADC_INDIRECT_Y(iterator.next());
                break;
            //ORA_ABS_X
            case 0x1D:
				cpuCycle-= cpuRegister.ORA_ABS_X(iterator.next(),iterator.next());
                break;
            //CMP_INDIRECT_Y
            case 0xD1:
				cpuCycle-= cpuRegister.CMP_INDIRECT_Y(iterator.next());
                break;
            //SBC_INDIRECT_Y
            case 0xF1:
				cpuCycle-= cpuRegister.SBC_INDIRECT_Y(iterator.next());
                break;
            //LDA_INDIRECT_X
            case 0xA1:
				cpuCycle-= cpuRegister.LDA_INDIRECT_X(iterator.next());
                break;
            //STA_INDIRECT_X
            case 0x81:
				cpuCycle-= cpuRegister.STA_INDIRECT_X(iterator.next());
                break;
            //INC_ABS_X
            case 0xFE:
				cpuCycle-= cpuRegister.INC_ABS_X(iterator.next(),iterator.next());
                break;
            //CPY_ABS
            case 0xCC:
				cpuCycle-= cpuRegister.CPY_ABS(iterator.next(),iterator.next());
                break;
            //ROR_ABS
            case 0x6E:
				cpuCycle-= cpuRegister.ROR_ABS(iterator.next(),iterator.next());
                break;
            //BVS
            case 0x70:
				cpuCycle-= cpuRegister.BVS(iterator.next());
                break;
            //ROL_ABS_X
            case 0x3E:
				cpuCycle-= cpuRegister.ROL_ABS_X(iterator.next(),iterator.next());
                break;
            //ASL_ABS_X
            case 0x1E:
				cpuCycle-= cpuRegister.ASL_ABS_X(iterator.next(),iterator.next());
                break;
            //EOR_ABS_X
            case 0x5D:
				cpuCycle-= cpuRegister.EOR_ABS_X(iterator.next(),iterator.next());
                break;
            //CPX_ABS
            case 0xEC:
				cpuCycle-= cpuRegister.CPX_ABS(iterator.next(),iterator.next());
                break;
            //ROR_ZERO_X
            case 0x76:
				cpuCycle-= cpuRegister.ROR_ZERO_X(iterator.next());
                break;
            //LSR_ZERO_X
            case 0x56:
				cpuCycle-= cpuRegister.LSR_ZERO_X(iterator.next());
                break;
            //ROL_ZERO_X
            case 0x36:
				cpuCycle-= cpuRegister.ROL_ZERO_X(iterator.next());
                break;
            //LDX_ZERO_Y
            case 0xB6:
				cpuCycle-= cpuRegister.LDX_ZERO_Y(iterator.next());
                break;
            //EOR_INDIRECT_Y
            case 0x51:
				cpuCycle-= cpuRegister.EOR_INDIRECT_Y(iterator.next());
                break;
            //STX_ZERO_Y
            case 0x96:
				cpuCycle-= cpuRegister.STX_ZERO_Y(iterator.next());
                break;
            //CLI
            case 0x58:
				cpuCycle-= cpuRegister.F_I((byte) 0);
                break;
            //LSR_ABS_X
            case 0x5E:
				cpuCycle-= cpuRegister.LSR_ABS_X(iterator.next(),iterator.next());
                break;
            default:
                System.out.printf("%02X\n",insCode);
                break;
        }
    }
}
