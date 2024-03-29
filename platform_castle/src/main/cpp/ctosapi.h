/********************************************************************
Copyright (c) Castles Technology Co., Ltd. 2003-2005

Module Name:
	ctosapi.h

Abstract:

Created:
	2005/11/25

Author:
	Peril Chen

Revision History:

*********************************************************************/
#ifndef __CTOS_API_H__
#define __CTOS_API_H__

#ifdef __cplusplus
extern "C"
{
#endif

#include "typedef.h"
#include "ctoserr.h"

//=============================================================================================================================
//
// ISystem Functions
//
//=============================================================================================================================
#define ID_CIF_KO					32

USHORT CTOS_GetSystemInfo(BYTE bID, BYTE baBuf[17]);

//=============================================================================================================================
//
// Clock and Time Functions
//
//=============================================================================================================================
// Real Time Clock define for RTC functions
typedef struct
{
    BYTE    bSecond;
    BYTE    bMinute;
    BYTE    bHour;
    BYTE    bDay;
    BYTE    bMonth;
    BYTE    bYear;
    BYTE    bDoW;
} CTOS_RTC;

//Time parameter
#define TIMER_ID_1		0
#define TIMER_ID_2		1
#define TIMER_ID_3		2
#define TIMER_ID_4		3

USHORT CTOS_RTCGet(CTOS_RTC *pstRTC);
USHORT CTOS_RTCSet(CTOS_RTC *pstRTC);
void CTOS_Delay(ULONG ulMSec);
ULONG CTOS_TickGet(void);
USHORT CTOS_TimeOutSet(BYTE bTID, ULONG ulMSec);
USHORT CTOS_TimeOutCheck(BYTE bTID);

//=============================================================================================================================
//
// Sound Functions
//
//=============================================================================================================================
USHORT CTOS_Beep(void);
USHORT CTOS_Sound(USHORT usFreq, USHORT usDuration);

//=============================================================================================================================
//
// File Functions
//
//=============================================================================================================================
// Storage Type
#define	d_STORAGE_FLASH						0
#define	d_STORAGE_SRAM						1

// Attribute
#define d_FA_PRIVATE							0
#define d_FA_READ									1		//	Read by other APs
#define d_FA_WRITE								2		//	Written by other APs
#define d_FA_PUBLIC								0xFF

#define d_SEEK_FROM_BEGINNING			0
#define	d_SEEK_FROM_CURRENT				1
#define	d_SEEK_FROM_EOF						2

USHORT CTOS_FileOpen(BYTE *caFileName , BYTE bStorageType, ULONG *pulFileHandle);
USHORT CTOS_FileOpenAttrib(BYTE *caFileName , BYTE bStorageType, ULONG *pulFileHandle, BYTE bAttrib);
USHORT CTOS_FileClose(ULONG ulFileHandle);
USHORT CTOS_FileDelete(BYTE *caFileName);
USHORT CTOS_FileGetSize(BYTE *caFileName, ULONG *pulFileSize);
USHORT CTOS_FileSeek(ULONG ulFileHandle, ULONG ulOffset, BYTE bOrigin);
USHORT CTOS_FileRead(ULONG ulFileHandle, BYTE *baBuffer, ULONG *pulActualLength);
USHORT CTOS_FileWrite(ULONG ulFileHandle, BYTE *baBuffer, ULONG ulBufferLength);
USHORT CTOS_FileDir(BYTE *baFileBuf, ULONG *pulFileSizeDir, USHORT *pusLen);

//=============================================================================================================================
//
// Contactless Functions
//
//=============================================================================================================================
// Type definition
#define d_CL_TYPE_A						0
#define d_CL_TYPE_B						1
#define d_CL_TYPE_C						2
 
// AntennaParam baudrate definition
#define d_CL_BR_106						0
#define d_CL_BR_212						1
#define d_CL_BR_424						2
#define d_CL_BR_848						3

// FSDI definition
#define d_CL_FSDI_16						0
#define d_CL_FSDI_24						1
#define d_CL_FSDI_32						2
#define d_CL_FSDI_40						3
#define d_CL_FSDI_48						4
#define d_CL_FSDI_64						5
#define d_CL_FSDI_96						6
#define d_CL_FSDI_128						7
#define d_CL_FSDI_256						8
#define d_CL_DFT_FSDI						8
#define d_CL_MAX_FSDI						8

// NumSlot definition
#define d_CL_NUMSLOT_x1						0x00
#define d_CL_NUMSLOT_x2						0x01
#define d_CL_NUMSLOT_x4						0x02
#define d_CL_NUMSLOT_x8						0x03
#define d_CL_NUMSLOT_x16					0x04

// MaxRate definition
#define d_CL_MAXRATE_106					106
#define d_CL_MAXRATE_212					212
#define d_CL_MAXRATE_424					424
#define d_CL_MAXRATE_848					848

// CL General
USHORT CTOS_CLInit(void);
USHORT CTOS_CLPowerOn(void);
USHORT CTOS_CLPowerOff(void);

// Mifare function
USHORT CTOS_MifareLOADKEY(UCHAR* baKey);
USHORT CTOS_MifareAUTHEx(UCHAR baKeyType,UCHAR bBlockNr,UCHAR* baCardSN,UCHAR bCardSNLen);
USHORT CTOS_MifareREADBLOCK(UCHAR bBlockNr,UCHAR* baBuf);
USHORT CTOS_MifareWRITEBLOCK(UCHAR bBlockNr,UCHAR* baBuf);

// ISO14443-3 Type B
USHORT CTOS_REQB(UCHAR bAFI,UCHAR* baATQB,UCHAR* bATQBLen);
USHORT CTOS_WUPB(UCHAR bAFI,UCHAR* baATQB,UCHAR* bATQBLen);
USHORT CTOS_ATTRIB(UCHAR* baPUPI,UCHAR* bRep);
USHORT CTOS_HALTB(BYTE *baPUPI);
USHORT CTOS_CLTypeBActiveEx(BYTE* baPUPI,UCHAR bAFI,BOOL fWUPB,UCHAR bFSDI,UCHAR bNumAntiColSlot,USHORT usMaxRate);
USHORT CTOS_CLTypeBActive(BYTE* baPUPI);

// ISO14443-4 T=CL
USHORT CTOS_CLRATS(BYTE bAutoBR,BYTE* baATS,USHORT* bATSLen);
USHORT CTOS_CLAPDU(BYTE* baSBuf,USHORT usSLen,BYTE* baRBuf,USHORT* usRLen);

// ISO18092 Felica
USHORT CTOS_FelicaPolling(UCHAR* IDm, UCHAR* PMm);

// TYPE A
USHORT CTOS_CLTypeAActiveFromIdle(BYTE bBaudRate,BYTE* baATQA,BYTE *bSAK,BYTE* baCSN,BYTE* bCSNLen);

//=============================================================================================================================
//
// Smart Card Functions
//
//=============================================================================================================================
// value for bVolt
#define d_SC_5V								1
#define d_SC_3V								2
#define d_SC_1_8V							3

USHORT CTOS_SCStatus(BYTE bID, BYTE *baStatus);
USHORT CTOS_SCPowerOff(BYTE bID);
USHORT CTOS_SCResetEMV(BYTE bID, BYTE bVolt, BYTE *baATR, BYTE *baATRLen, BYTE *baCardType);
USHORT CTOS_SCResetISO(BYTE bID, BYTE bVolt, BYTE *baATR, BYTE *baATRLen, BYTE *baCardType);
USHORT CTOS_SCWarmResetEMV(BYTE bID, BYTE bVolt, BYTE *baATR, BYTE *baATRLen, BYTE *baCardType);
USHORT CTOS_SCWarmResetISO(BYTE bID, BYTE bVolt, BYTE *baATR, BYTE *baATRLen, BYTE *baCardType);
USHORT CTOS_SCCommonReset(BYTE bID, BYTE bTA1, BOOL fColdReset, BOOL fEMV, BOOL fPTS, BOOL fIFSD, BYTE bVolt, BYTE *baATR, BYTE *baATRLen, BYTE *baCardType);
USHORT CTOS_SCSendAPDU(BYTE bID, BYTE *baSBuf, USHORT usSLen, BYTE *baRBuf, USHORT *pusRLen);
	
//=============================================================================================================================
//
// MSR Functions
//
//=============================================================================================================================
USHORT CTOS_MSRRead(BYTE*baTk1Buf, USHORT*pusTk1Len, BYTE*baTk2Buf, USHORT*pusTk2Len, BYTE*baTk3Buf, USHORT*pusTk3Len);
USHORT CTOS_MSRGetLastErr (BYTE*baTk1Err, BYTE*baTk2Err, BYTE*baTk3Err);

//=============================================================================================================================
//
// Power Saving Functions
//
//=============================================================================================================================
// value for bMode
#define d_PWR_STANDBY_MODE				0x00
#define d_PWR_SLEEP_MODE				0x01
#define d_PWR_REBOOT					0x02
#define d_PWR_POWER_OFF					0x03

USHORT CTOS_PowerSource(UCHAR* bSrc);
USHORT CTOS_PowerMode(BYTE bMode);
void CTOS_SystemReset(void);
USHORT CTOS_SystemWait(DWORD dwTimeout, DWORD dwWaitEvent, DWORD* pdwWakeupEvent);
USHORT CTOS_PowerAwakening(SHORT sSecond, SHORT sMinute, SHORT sHour, SHORT sDay, SHORT sMonth, SHORT sYear);

//=============================================================================================================================
//
// CRYPTO Functions
//
//=============================================================================================================================


#define SHA_CTX SHA1Context

typedef struct SHA1Context
{
    unsigned long Message_Digest[5];
    unsigned long Length_Low;
    unsigned long Length_High;
    unsigned char Message_Block[64];
    int Message_Block_Index;
    int Computed;
    int Corrupted;
} SHA1Context;


typedef struct
{
    IN BYTE Version;
    IN USHORT bits;
    OUT USHORT m_len;
    OUT BYTE *m;
    OUT USHORT d_len;
    OUT BYTE *d;
    IN  USHORT e_len;
    IN  BYTE *e;
}CTOS_RSA_KEY_GEN;


#define SHA256_BLOCK_SIZE  ( 512 / 8)
#define SHA256_CTX stSHA256_ctx
#define CTOS_SHA256_CTX stSHA256_ctx
typedef struct {
    unsigned int tot_len;
    unsigned int len;
    unsigned char block[2 * SHA256_BLOCK_SIZE];
    ULONG h[8];
} stSHA256_ctx;



USHORT CTOS_RNG(BYTE *baResult);
USHORT CTOS_DES (BYTE bEncDec, BYTE* baKey, BYTE bKeyLen, BYTE* baData, USHORT usDataLen, BYTE* baResult);
USHORT CTOS_DES_CBC (BYTE bEncDec, BYTE* baKey, BYTE bKeyLen, BYTE* baICV, BYTE* baData, USHORT usDataLen, BYTE* baResult);
USHORT CTOS_AES_ECB (BYTE bEncDec, BYTE* baKey, BYTE bKeyLen, BYTE* baData, USHORT usDataLen, BYTE* baResult);
USHORT CTOS_AES_CBC (BYTE bEncDec, BYTE* baKey, BYTE bKeyLen, BYTE* baICV, BYTE* baData, USHORT usDataLen, BYTE* baResult);
USHORT CTOS_MAC (BYTE* baKey, BYTE bKeyLen, BYTE* baICV, BYTE* baData, USHORT usDataLen, BYTE* baMAC);
void CTOS_SHA1Init(SHA_CTX *pstInfo);
void CTOS_SHA1Update(SHA_CTX *pstInfo, BYTE* baBuffer, USHORT usCount);
void CTOS_SHA1Final(BYTE* baOutput, SHA_CTX *pstInfo);
void CTOS_SHA1(BYTE *baBuffer, USHORT usCount, BYTE *baOutput);
void CTOS_SHA256Init(SHA256_CTX *ctx);
void CTOS_SHA256Update(SHA256_CTX *ctx, const BYTE *message, UINT len);
void CTOS_SHA256Final(SHA256_CTX *ctx, BYTE *digest);
void CTOS_SHA256(const BYTE *message, UINT len, BYTE *digest);
USHORT CTOS_RSA(BYTE *baModulus, USHORT usModulousLen, BYTE *baExponent, USHORT usExponentLen, BYTE *baData, BYTE *baResult);
USHORT CTOS_RSAKeyGenerate(CTOS_RSA_KEY_GEN *para);




//=============================================================================================================================
//
// KMS2 Functions
//
//=============================================================================================================================

/****************************************************************************
 * For CTOS_KMS2KeySwap Function                                            *
 ****************************************************************************/
typedef struct
{
    // Should be 0x00 or 0x01
    IN BYTE Version;

    struct
    {
        IN USHORT KeySet;

        IN USHORT KeyIndex;

    }Source1;

    struct
    {
        IN USHORT KeySet;

        IN USHORT KeyIndex;

    }Source2;

}CTOS_KMS2KEYSWAP_PARA;

/****************************************************************************
 * For CTOS_KMS2DATAENCRYPT Function                                        *
 ****************************************************************************/
typedef struct
{
    // Should be 0x00 or 0x01
    IN BYTE Version;

    struct
    {
        IN USHORT CipherKeySet;
        IN USHORT CipherKeyIndex;
        IN BYTE CipherMethod;

        // This is used for KeyType is KMS2_KEYTYPE_3DES/KMS2_KEYTYPE_AES
        // If SK_Length is 0, SK will not be calculated and used.
        IN BYTE SK_Length;
        IN BYTE* pSK;

    }Protection;

    // This is used for KeyType is KMS2_KEYTYPE_3DES_DUKPT
    struct
    {
        IN BOOL IsUseCurrentKey;
    }DUKPT_PARA;

    struct
    { 
        IN USHORT Length;
        IN BYTE* pData;

        IN USHORT ICVLength;
        IN BYTE* pICV;
    }Input;

    // This is not used if CipherMethod is KMS2_DATAENCRYPTCIPHERMETHOD_EXTPIN_ECB
    struct
    {
        OUT USHORT Length;
        OUT BYTE* pData;
        OUT BYTE KSNLen;
        OUT BYTE* pKSN;
    }Output;

}CTOS_KMS2DATAENCRYPT_PARA;

/****************************************************************************
 * For CTOS_KMS2MAC Function                                                *
 ****************************************************************************/
typedef struct
{
    // Should be 0x00 or 0x01
    IN BYTE Version;

    struct
    {
        IN USHORT CipherKeySet;
        IN USHORT CipherKeyIndex;
        IN BYTE CipherMethod;

        // This is used for KeyType is KMS2_KEYTYPE_3DES/KMS2_KEYTYPE_AES
        // If SK_Length is 0, SK will not be calculated and used.
        IN BYTE SK_Length;
        IN BYTE* pSK;
    }Protection;

    struct
    {
        BYTE Length;
        BYTE* pData;
    }ICV;

    // This is used for KeyType is KMS2_KEYTYPE_3DES_DUKPT
    struct
    {
        IN BOOL IsUseCurrentKey;
    }DUKPT_PARA;

    struct
    {
        IN USHORT Length;
        IN BYTE* pData;
    }Input;

    struct
    {
        OUT USHORT Length;
        OUT BYTE* pData;
        OUT BYTE KSNLen;
        OUT BYTE* pKSN;
    }Output;

}CTOS_KMS2MAC_PARA;

/****************************************************************************
 * For CTOS_KMS2KeyGetInfo Function                                         *
 ****************************************************************************/
typedef struct
{
    // Should be 0x00 or 0x01
    IN BYTE Version;

    struct
    {
        IN USHORT KeySet;

        IN USHORT KeyIndex;

        // Only used for KeyType 3DES/3DES-DUKPT/AES
        IN BYTE CVLen;

        // Only used for KeyType RSA
        IN BYTE HashAlgorithm;

    }Input;

    struct
    {
        OUT BYTE KeyType;
        OUT BYTE KeyVersion;
        OUT DWORD KeyAttribute;
        OUT USHORT KeyLength;

        // Only used for KeyType 3DES/3DES-DUKPT/AES
        OUT BYTE* pCV;

        // Only used for KeyType RSA
        OUT USHORT KeyExponentLength;

        // Only used for KeyType RSA
        // Calculated with the following input data in order:
        //      Modulus Length - 2 bytes, MSB to LSB
        //      Modulus
        //      Exponent Length - 2 bytes, MSB to LSB
        //      Exponent
        OUT BYTE* pHash;
    }Output;

}CTOS_KMS2KEYGETINFO_PARA;

/****************************************************************************
 * For CTOS_KMS2KeyWriteByTR31 Function                                     *
 ****************************************************************************/
typedef struct
{
    // Should be 0x00 or 0x01
    IN BYTE Version;

    struct
    {
        IN USHORT KeySet;
        IN USHORT KeyIndex;

    }Info;

    struct
    {
        IN USHORT CipherKeySet;
        IN USHORT CipherKeyIndex;

    }Protection;

    struct
    {
        IN USHORT KeyLength;
        IN BYTE* pKeyData;

    }Value;

}CTOS_KMS2KEYWRITEBYTR31_PARA;

/****************************************************************************
 * For SB_KMS2KEYWRITE_PARA Function                                        *
 ****************************************************************************/
typedef struct
{
    // Should be 0x00 or 0x01   
    IN BYTE Version;
    
    struct
    {               
        // Note that 0x0000 and 0xFF00 to 0xFFFF are reserved for system.
        IN USHORT KeySet;
        
        IN USHORT KeyIndex;
        
        IN BYTE KeyType;        
                        
        IN BYTE KeyVersion; 
        
        // For KeyType RSA, only accepts KMS2_KEYATTRIBUTE_PROTECTED
        IN DWORD KeyAttribute;
    }Info;
    
    // This is not used for KeyType RSA
    struct
    {
        IN BYTE Mode;
        
        // This is used for KPK_ECB and TR31 Mode
        IN USHORT CipherKeySet;
        IN USHORT CipherKeyIndex;
        
        struct
        {
            // This is used for TR31 Mode
            // This is used as ICV for SELF_UPDATE_CBC mode
            IN USHORT Length;
            IN BYTE* pData;
        }AdditionalData;
                        
    }Protection;            
    
    struct
    {
        IN USHORT KeyLength;
        // For KeyType RSA, this is used for Modulus.
        IN BYTE* pKeyData;
    }Value;
    
    // This is not used if Protection.Mode is KMS2_KEYPROTECTIONMODE_PLAINTEXT
    // This is not used for KeyType RSA
    struct
    {
        IN BYTE Method;
        IN USHORT KeyCheckValueLength;
        IN BYTE* pKeyCheckValue;
    }Verification;
    
    // Optional. Used only for KeyType RSA
    struct
    {
        IN USHORT Length;
        IN BYTE* pValue; 
    }Exponent;
    
    // Optional. Used only for KeyType 3DES_DUKPT
    struct
    {       
        IN BYTE KSNLength;
        IN BYTE *pKSN;
    }DUKPT_DATA;
    
}CTOS_KMS2KEYWRITE_PARA;
/****************************************************************************
 * For CTOS_KMS2PINGET_PARA Function                                        *
 ****************************************************************************/
typedef struct
{
    // Should be 0x00 or 0x01
    IN BYTE Version;

    struct
    {
        IN BYTE BlockType;
        IN BYTE PINDigitMaxLength;
        IN BYTE PINDigitMinLength;
    }PIN_Info;

    struct
    {
        IN USHORT CipherKeySet;
        IN USHORT CipherKeyIndex;
        IN BYTE CipherMethod;

        // This is used for KeyType is KMS2_KEYTYPE_3DES/KMS2_KEYTYPE_AES
        // If SK_Length is 0, SK will not be calculated and used.
        IN BYTE SK_Length;
        IN BYTE* pSK;
    }Protection;

    struct
    {
        // This is used for PAN if BlockType is KMS2_PINBLOCKTYPE_ANSI_X9_8_ISO_0.
        // Ths is used for terminal pseudo random if BlockType  is KMS2_PINBLOCKTYPE_ISBAN_REVERSIBLE_PIN_4B_MODE.
        // This is used for PK if BlockType is KMS2_PINBLOCKTYPE_ISBAN_IRREVERSIBLE_PIN_4B_MODE.
        IN BYTE InLength;
        IN BYTE* pInData;

    }AdditionalData;

    // This is used for KeyType is KMS2_KEYTYPE_3DES_DUKPT
    struct
    {
        IN BOOL IsUseCurrentKey;
    }DUKPT_PARA;

    struct
    {
        INOUT USHORT EncryptedBlockLength;
        OUT BYTE* pEncryptedBlock;
        OUT BYTE PINDigitActualLength;
    }PINOutput;

    struct
    {
        IN DWORD Timeout;
        IN BYTE AsteriskPositionX;
        IN BYTE AsteriskPositionY;
        IN BYTE NULLPIN;
        IN int (*piTestCancel)(void);
    }Control;

}CTOS_KMS2PINGET_PARA;

typedef struct {

    struct {
        IN USHORT KeyUsage;
        IN BYTE ModeofUse;
        IN BYTE KeyVersionNumber;
    } KeyInfo;

    struct {
        IN USHORT KeyLength;
        IN BYTE *pKeyData;
    } Value;

    struct {
        IN USHORT CipherKeyLength;
        IN BYTE *pCipherKeyData;
    } Protection;

    struct {
        IN USHORT KSNLength;
        IN BYTE *pKSN;
    } KSN;

    struct {
        OUT USHORT OutputLength;
        OUT BYTE *pOutputData;
    } Output;

} TR31_KEYBLOCK;

USHORT CTOS_KMS2Init (void);
USHORT CTOS_KMS2KeyCheck(USHORT ikeyset, USHORT ikeyindex);
USHORT CTOS_KMS2KeyDelete(USHORT ikeyset, USHORT ikeyindex);
USHORT CTOS_KMS2KeyCheckAll (void);
USHORT CTOS_KMS2KeyDeleteAll (void);
USHORT CTOS_KMS2KeySwap(CTOS_KMS2KEYSWAP_PARA *para);
USHORT CTOS_KMS2KeyGetInfo(CTOS_KMS2KEYGETINFO_PARA *pKeyGetInfoPara);
USHORT CTOS_KMS2DataEncrypt(CTOS_KMS2DATAENCRYPT_PARA *pDataEncPara);
USHORT CTOS_KMS2MAC(CTOS_KMS2MAC_PARA *pMacPara);
USHORT CTOS_KMS2UserDataWrite(BOOL isCommon, UINT offset, BYTE* baInBuf, UINT bufLen);
USHORT CTOS_KMS2UserDataRead(BOOL isCommon, UINT offset, BYTE* baOutBuf, UINT bufLen);
USHORT CTOS_KMS2KeyWriteByTR31(CTOS_KMS2KEYWRITEBYTR31_PARA* pKeyWritebyTR31Para);
USHORT CTOS_KMS2DUKPTGetKSN(USHORT iKeySet, USHORT iKeyIndex, BYTE* baOutBuf, UINT* bufLen);
USHORT CTOS_KMS2PINGet(CTOS_KMS2PINGET_PARA *pPinGetPara);
USHORT CTOS_KMS2KeyWrite(CTOS_KMS2KEYWRITE_PARA *pKeyWritePara);

//=============================================================================================================================
//
// Font Functions
//
//=============================================================================================================================

typedef struct
{
    USHORT FontSize;
    USHORT X_Zoom;
    USHORT Y_Zoom;
    USHORT X_Space;
    USHORT Y_Space;
} CTOS_FONT_ATTRIB;

#define d_PRT_ASCII_DEFAULT    0    //check
#define d_PRT_ASCII_BOLD       1    //check
//----------------------------------------------------------------------------------------Mark_Bottom
#define d_UTF_16
#define d_FONT_MAX_PAT_LEN  255*32 //support maximum font size 255*255

#define d_FNT_MODE          0
#define d_TTF_NORM_MODE     1
#define d_TTF_HB_MODE       2
//-----------------------------------
#define d_LANGUAGE_DEFAULT    0
#define d_LANGUAGE_KHMER      1
#define d_LANGUAGE_ARABIC     2
#define d_LANGUAGE_THAI       3
//-----------------------------------
// font device definition
#define d_FONT_DEVICE_LCD_0                                                 0x00
#define d_FONT_DEVICE_LCD_1                                                 0x01
#define d_FONT_DEVICE_PRINTER                                               0x80

// FNT Lauguage definition
#define d_FNT_LANGUAGE_ALPHABET                                             0x0000
#define d_FNT_LANGUAGE_CHINESE_TRADITIONAL                                  0x0001  //Chinese(Traiditional)
#define d_FNT_LANGUAGE_CHINESE_SIMPLIFIED                                   0x0002    //Chinese(Simplified)
#define d_FNT_LANGUAGE_JAPANESE                                             0x0003    //Japanese
#define d_FNT_LANGUAGE_THAI                                                 0x0004    //Thai
#define d_FNT_LANGUAGE_VIETNAMESE                                           0x0005    //Vietnamese
#define d_FNT_LANGUAGE_KOREAN                                               0x0006    //Korean
#define d_FNT_LANGUAGE_RUSSIAN                                              0x0007  //Russian
#define d_FNT_LANGUAGE_PORTUGUESE_PORTUGAL                                  d_FNT_LANGUAGE_ALPHABET
#define d_FNT_LANGUAGE_SPANISH                                              d_FNT_LANGUAGE_ALPHABET
#define d_FNT_LANGUAGE_TURKISH                                              d_FNT_LANGUAGE_ALPHABET
#define d_FNT_LANGUAGE_FARSI                                                d_FNT_LANGUAGE_ALPHABET

#define d_FNT_LANGUAGE_PORTUGUESE_BRAZIL                                    d_FNT_LANGUAGE_ALPHABET
#define d_FNT_LANGUAGE_CZECH                                                d_FNT_LANGUAGE_ALPHABET
#define d_FNT_LANGUAGE_ARABIC_QATAR                                         d_FNT_LANGUAGE_ALPHABET

// FNT FontID definition
#define d_FNT_FONTID_EMPTY                                                  0       //No font
#define d_FNT_FONTID_CHINESE_TAIWAN                                         1028    //Chinese(Taiwan)
#define d_FNT_FONTID_CZECH                                                  1029    //Czech
#define d_FNT_FONTID_JAPANESE                                               1041    //Japanese
#define d_FNT_FONTID_KOREAN                                                 1042    //Korean
#define d_FNT_FONTID_PORTUGUESE_BRAZIL                                      1046    //Portuguese(Brazil)
#define d_FNT_FONTID_RUSSIAN                                                1049    //Russian
#define d_FNT_FONTID_THAI                                                   1054    //Thai
#define d_FNT_FONTID_TURKISH                                                1055    //Turkish
#define d_FNT_FONTID_FARSI                                                  1065    //Farsi
#define d_FNT_FONTID_VIETNAMESE                                             1066    //Vietnamese
#define d_FNT_FONTID_CHINESE_PRC                                            2052    //Chinese(PRC)
#define d_FNT_FONTID_CHINESE_PRC1                                           2054    //Simple(Song)
#define d_FNT_FONTID_PORTUGUESE_PORTUGAL                                    2070    //Portuguese(Portugal)
#define d_FNT_FONTID_SPANISH                                                3082    //Spanish(Spain)
#define d_FNT_FONTID_ARABIC_QATAR                                           16385   //Arabic(Qatar)
#define d_FNT_FONTID_CHINESE_TAIWAN_PLUS                                    64507   //Chinese(Taiwan) with Symbol
#define d_FNT_FONTID_DEFAULTASCII                                           36864

// Font language
// ASCII < 0x80 is world-wide same
// ASCII >=0x80 is language dependent
#define d_TTF_C0_CONTROLS                                         0x0000
#define d_TTF_BASIC_LATIN                                         0x0020
#define d_TTF_C1_CONTROLS                                         0x0080
#define d_TTF_LATIN_1_SUPPLEMENT                                  0x00A0
#define d_TTF_LATIN_EXTENDED_A                                    0x0100
#define d_TTF_LATIN_EXTENDED_B                                    0x0180
#define d_TTF_IPA_EXTENSIONS                                      0x0250
#define d_TTF_SPACING_MODIFIERS                                   0x02B0
#define d_TTF_COMBINING_DIACRITICS_MARKS                          0x0300
#define d_TTF_GREEK_AND_COPTIC                                    0x0370
#define d_TTF_CYRILLIC                                            0x0400
#define d_TTF_CYRILLIC_SUPPLEMENT                                 0x0500
#define d_TTF_ARMENIAN                                            0x0530
#define d_TTF_HEBREW                                              0x0590
#define d_TTF_ARABIC                                              0x0600
#define d_TTF_SYRIAC                                              0x0700
#define d_TTF_ARABIC_SUPPLEMENT                                   0x0750
#define d_TTF_THAANA                                              0x0780
#define d_TTF_N_KO                                                0x07C0
#define d_TTF_SAMARITAN                                           0x0800
#define d_TTF_MANDAIC                                             0x0840
#define d_TTF_DEVANAGARI                                          0x0900
#define d_TTF_BENGALI                                             0x0980
#define d_TTF_GURMUKHI                                            0x0A00
#define d_TTF_GUJARATI                                            0x0A80
#define d_TTF_ORIYA                                               0x0B00
#define d_TTF_TAMIL                                               0x0B80
#define d_TTF_TELUGU                                              0x0C00
#define d_TTF_KANNADA                                             0x0C80
#define d_TTF_MALAYALAM                                           0x0D00
#define d_TTF_SINHALA                                             0x0D80
#define d_TTF_THAI                                                0x0E00
#define d_TTF_LAO                                                 0x0E80
#define d_TTF_TIBETAN                                             0x0F00
#define d_TTF_MYANMAR                                             0x1000
#define d_TTF_GEORGIAN                                            0x10A0
#define d_TTF_HANGUL_JAMO                                         0x1100
#define d_TTF_ETHIOPIC                                            0x1200
#define d_TTF_ETHIOPIC_SUPPLEMENT                                 0x1380
#define d_TTF_CHEROKEE                                            0x13A0
#define d_TTF_UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS               0x1400
#define d_TTF_OGHAM                                               0x1680
#define d_TTF_RUNIC                                               0x16A0
#define d_TTF_TAGALOG                                             0x1700
#define d_TTF_HANUNOO                                             0x1720
#define d_TTF_BUHID                                               0x1740
#define d_TTF_TAGBANWA                                            0x1760
#define d_TTF_KHMER                                               0x1780
#define d_TTF_MONGOLIAN                                           0x1800
#define d_TTF_UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS_EXTENDED      0x18B0
#define d_TTF_LIMBU                                               0x1900
#define d_TTF_TAI_LE                                              0x1950
#define d_TTF_NEW_TAI_LUE                                         0x1980
#define d_TTF_KHMER_SYMBOLS                                       0x19E0
#define d_TTF_BUGINESE                                            0x1A00
#define d_TTF_TAI_THAM                                            0x1A20
#define d_TTF_BALINESE                                            0x1B00
#define d_TTF_SUNDANESE                                           0x1B80
#define d_TTF_BATAK                                               0x1BC0
#define d_TTF_LEPCHA                                              0x1C00
#define d_TTF_OL_CHIKI                                            0x1C50
#define d_TTF_SUDANESE_SUPPLEMENT                                 0x1CC0
#define d_TTF_VEDIC_EXTENSIONS                                    0x1CD0
#define d_TTF_PHONETIC_EXTENSIONS                                 0x1D00
#define d_TTF_PHONETIC_EXTENSIONS_SUPPLEMENT                      0x1D80
#define d_TTF_COMBINING_DIACRITICS_MARKS_SUPPLEMENT               0x1DC0
#define d_TTF_LATIN_EXTENDED_ADDITIONAL                           0x1E00
#define d_TTF_GREEK_EXTENDED                                      0x1F00
#define d_TTF_GENERAL_PUNCTUATION                                 0x2000
#define d_TTF_SUPERSCRIPTS_AND_SUBSCRIPTS                         0x2070
#define d_TTF_CURRENCY_SYMBOLS                                    0x20A0
#define d_TTF_COMBINING_DIACRITICS_MARKS_FOR_SYMBOLS              0x20D0
#define d_TTF_LETTERLIKE_SYMBOLS                                  0x2100
#define d_TTF_NUMBER_FORM                                         0x2150
#define d_TTF_ARROWS                                              0x2190
#define d_TTF_MATHEMATICAL_OPERATOR                               0x2200
#define d_TTF_MISCELLANEOUS_TECHNICAL                             0x2300
#define d_TTF_CONTROL_PICTURES                                    0x2400
#define d_TTF_OPTICAL_CHARACTER_RECOGNITION                       0x2440
#define d_TTF_ENCLOSED_ALPHANUMERICS                              0x2460
#define d_TTF_BOX_DRAWING                                         0x2500
#define d_TTF_BLOCK_ELEMENT                                       0x2580
#define d_TTF_GEOMETRIC_SHAPES                                    0x25A0
#define d_TTF_MISCELLANEOUS_SYMBOLS                               0x2600
#define d_TTF_DINGBATS                                            0x2700
#define d_TTF_MISCELLANEOUS_MATHEMATICAL_SYMBOLS_A                0x27C0
#define d_TTF_SUPPLEMENTAL_ARROWS_A                               0x27F0
#define d_TTF_BRAILLE_PATTERNS                                    0x2800
#define d_TTF_SUPPLEMENTAL_ARROWS_B                               0x2900
#define d_TTF_MISCELLANEOUS_MATHEMATICAL_SYMBOLS_B                0x2980
#define d_TTF_SUPPLEMENTAL_MATHEMATICAL_OPERATOR                  0x2A00
#define d_TTF_MISCELLANEOUS_SYMBOLS_AND_ARROWS                    0x2B00
#define d_TTF_GLAGOLITIC                                          0x2C00
#define d_TTF_LATIN_EXTENDED_C                                    0x2C60
#define d_TTF_COPTIC                                              0x2C80
#define d_TTF_GEORGIAN_SUPPLEMENT                                 0x2D00
#define d_TTF_TIFINAGH                                            0x2D30
#define d_TTF_ETHIOPIC_EXTENDED                                   0x2D80
#define d_TTF_SUPPLEMENTAL_PUNCTUATION                            0x2E00
#define d_TTF_CJK_RADICALS_SUPPLEMENT                             0x2E80
#define d_TTF_KANGXI_RADICALS                                     0x2F00
#define d_TTF_IDEOGRAPHIC_DESCRIPTION_CHARACTERS                  0x2FF0
#define d_TTF_CJK_SYMBOLS_AND_PUNCTUATION                         0x3000
#define d_TTF_HIRAGANA                                            0x3040
#define d_TTF_KATAKANA                                            0x30A0
#define d_TTF_BOPOMOFO                                            0x3100
#define d_TTF_HANGUL_COMPATIBILITY_JAMO                           0x3130
#define d_TTF_KANBUN                                              0x3190
#define d_TTF_BOPOMOFO_EXTENDED                                   0x31A0
#define d_TTF_CJK_STROKES                                         0x31C0
#define d_TTF_KATAKANA_PHONETIC_EXTENSIONS                        0x31F0
#define d_TTF_ENCLOSED_CJK_LETTERS_AND_MONTHS                     0x3200
#define d_TTF_CJK_COMPATIBILITY                                   0x3300
#define d_TTF_CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A                  0x3400
#define d_TTF_YIJING_HEXAGRAMS_SYMBOLS                            0x4DC0
#define d_TTF_CJK_UNIFIED_IDEOGRAPHS                              0x4E00
#define d_TTF_YI_SYLLABLES                                        0xA000
#define d_TTF_YI_RADICALS                                         0xA490
#define d_TTF_LISU                                                0xA4D0
#define d_TTF_VAI                                                 0xA500
#define d_TTF_CYRILLIC_EXTENDED_B                                 0xA640
#define d_TTF_BAMUM                                               0xA6A0
#define d_TTF_MODIFIER_TONE_LETTERS                               0xA700
#define d_TTF_LATIN_EXTENDED_D                                    0xA720
#define d_TTF_SYLOTI_NAGRI                                        0xA800
#define d_TTF_IND_NO                                              0xA830
#define d_TTF_PHAGS_PA                                            0xA840
#define d_TTF_SAURASHTRA                                          0xA880
#define d_TTF_DEVA_EXT                                            0xA8E0
#define d_TTF_KAYAH_LI                                            0xA900
#define d_TTF_REJANG                                              0xA930
#define d_TTF_JAVANESE                                            0xA980
#define d_TTF_CHAM                                                0xAA00
#define d_TTF_MYANMAR_EXTA                                        0xAA60
#define d_TTF_TAI_VIET                                            0xAA80
#define d_TTF_MEETEI_EXT                                          0xAAE0
#define d_TTF_ETHIOPIC_EXT_A                                      0xAB00
#define d_TTF_MEETEI_MAYEK                                        0xABC0
#define d_TTF_HANGUL_SYLLABLES                                    0xAC00
#define d_TTF_HANGUL_JAMO_EXTENDED_B                              0xD7B0
#define d_TTF_HIGH_HALF_ZONE_OF_UTF_16                            0xD800
#define d_TTF_LOW_HALF_ZONE_OF_UTF_16                             0xDC00
#define d_TTF_PRIVATE_USE_ZONE                                    0xE000
#define d_TTF_CJK_COMPATIBILITY_IDEOGRAPHS                        0xF900
#define d_TTF_ALPHABETIC_PRESENTATION_FORMS                       0xFB00
#define d_TTF_ARABIC_PRESENTATION_FORMS_A                         0xFB50
#define d_TTF_VARIATION_SELECTOR                                  0xFE00
#define d_TTF_VERTICAL_FORMS                                      0xFE10
#define d_TTF_COMBINING_HALF_MARKS                                0xFE20
#define d_TTF_CJK_COMPATIBILITY_FORMS                             0xFE30
#define d_TTF_SMALL_FORM_VARIANTS                                 0xFE50
#define d_TTF_ARABIC_PRESENTATION_FORMS_B                         0xFE70
#define d_TTF_HALFWIDTH_AND_FULLWIDTH_FORMS                       0xFF00
#define d_TTF_SPECIALS                                            0xFFF0

#define d_FONT_EMPTY                                              0       //No font
#define d_FONT_CHINESE_TAIWAN                                     1028    //Chinese(Taiwan)
#define d_FONT_CZECH                                              1029    //Czech
#define d_FONT_JAPANESE                                           1041    //Japanese
#define d_FONT_KOREAN                                             1042    //Korean
#define d_FONT_PORTUGUESE_BRAZIL                                  1046    //Portuguese(Brazil)
#define d_FONT_RUSSIAN                                            1049    //Russian
#define d_FONT_THAI                                               1054    //Thai
#define d_FONT_TURKISH                                            1055    //Turkish
#define d_FONT_FARSI                                              1065    //Farsi
#define d_FONT_VIETNAMESE                                         1066    //Vietnamese
#define d_FONT_CHINESE_PRC                                        2052    //Chinese(PRC)
#define d_FONT_CHINESE_PRC1                                       2054    //Simple(Song)
#define d_FONT_PORTUGUESE_PORTUGAL                                2070    //Portuguese(Portugal)
#define d_FONT_SPANISH                                            3082    //Spanish(Spain)
#define d_FONT_ARABIC_QATAR                                       16385   //Arabic(Qatar)
#define d_FONT_CHINESE_TAIWAN_PLUS                                64507   //Chinese(Taiwan) with Symbol
#define d_FONT_DEFAULTASCII                                       36864
//SwitchDisplayMode()
#define d_TTF_MODE_DEFAULT                                        0x0000
#define d_TTF_MODE_VIETNAM                                        0xA001
#define d_TTF_MODE_BURMA                                          0xA002
#define d_TTF_MODE_KHMER                                          0xA003
#define d_TTF_MODE_SPAIN                                          0xA004

// for font usage
#define d_FONT_LCD_USE                                            0x00
#define d_FONT_PRINTER_USE                                        0x01
#define d_FONT_FNT_MODE                                           0
#define d_FONT_TTF_MODE                                           1
#define d_FONT_DEFAULT_TTF                                        "ca_default.ttf"

// font style definition
#define d_FONT_STYLE_NORMAL                                                 0x0000
#define d_FONT_STYLE_REVERSE                                                0x0001
#define d_FONT_STYLE_UNDERLINE                                              0x0002
#define d_FONT_STYLE_BOLD                                                   0x0004
#define d_FONT_STYLE_ITALIC                                                 0x0010

// Font size
#define d_FONT_8x8                                                0x0808
#define d_FONT_8x16                                               0x0810
#define d_FONT_16x16                                              0x1010
#define d_FONT_12x24                                              0x0C18
#define d_FONT_24x24                                              0x1818
#define d_FONT_9x9                                                0x0909
#define d_FONT_9x18                                               0x0912
#define d_FONT_16x30                                              0x101E
#define d_FONT_20x40                                              0x1428

// for IPrinter font type set
#define d_FONT_NO_SET_TYPE                                        0
#define d_FONT_TYPE1                                              1
#define d_FONT_TYPE2                                              2
#define d_FONT_TYPE3                                              3
//==================================================
//-------------------------------------------------------------------------
//    LCD
//-------------------------------------------------------------------------
USHORT CTOS_FontSelectMode(BYTE bDevice,BYTE bMode);
USHORT CTOS_FontFNTNum(USHORT* pusIndex);
USHORT CTOS_FontFNTInfo(USHORT usIndex,USHORT* pusFontID,USHORT* pusFontSize);
USHORT CTOS_LibCAFontGetVersion(BYTE* verion_str);
USHORT CTOS_FontFNTSelectLanguage(BYTE bDevice,USHORT usLanguage);
USHORT CTOS_FontFNTSelectFont(BYTE bDevice,USHORT usFontID);
USHORT CTOS_FontFNTSelectASCIIFont(BYTE bDevice,USHORT usFontID);
USHORT CTOS_FontFNTSelectStyle(BYTE bDevice,USHORT usStyle);
USHORT CTOS_FontFNTSelectSize(BYTE bDevice,USHORT usFontSize);
USHORT CTOS_FontTTFCheckSupport(ULONG ulEncoding, BOOL *fSupported );
USHORT CTOS_FontTTFSelectFontFile(BYTE bDevice,BYTE *baFontFileName, BYTE bIndex);
USHORT CTOS_FontTTFSelectStyle(BYTE bDevice,USHORT usStyle);
USHORT CTOS_FontTTFSelectSize(BYTE bDevice,USHORT usFontSize);
USHORT CTOS_FontTTFSwitchDisplayMode(BYTE bDevice, USHORT usMode);
USHORT CTOS_FontTTFSwichDisplayMode(BYTE bDevice, USHORT usMode);
USHORT CTOS_LanguageConfig(USHORT usLanguage,USHORT usFontSize,USHORT usFontStyle, BOOL boSetDefault);
USHORT CTOS_LanguageLCDFontSize(USHORT usFontSize,USHORT usFontStyle);
USHORT CTOS_LanguagePrinterFontSize(USHORT usFontSize,USHORT usFontStyle, USHORT usSetType);
USHORT CTOS_LanguageInfo(USHORT usIndex,USHORT* pusLanguage,USHORT* pusFontSize,USHORT* pusFontStyle);
USHORT CTOS_LanguageNum(USHORT* pusIndex);
USHORT CTOS_LanguagePrinterSelectASCII(USHORT usASCIIFontID);
USHORT CTOS_LanguageLCDSelectASCII(USHORT usASCIIFontID);
USHORT CTOS_LanguagePrinterGetFontInfo(USHORT* pusASCIIFontID,USHORT* pusFontSize,USHORT* pusFontStyle);
USHORT CTOS_LanguageLCDGetFontInfo(USHORT* pusASCIIFontID,USHORT* pusFontSize,USHORT* pusFontStyle);
//-------------------------------------------------------------------------
//    IPrinter
//-------------------------------------------------------------------------
#define d_PRINTER_ALIGNLEFT	0
#define d_PRINTER_ALIGNCENTER	1
#define d_PRINTER_ALIGNRIGHT	2
// for IPrinter function
#define PAPER_X_SIZE			384

#define PB_CANVAS_X_SIZE			384
#define PB_CANVAS_Y_SIZE 			80
#define MAX_PB_CANVAS_NUM			2

USHORT CTOS_PrinterSetDefaultASCIIStyle( UCHAR bStyle);
USHORT CTOS_PrinterBufferInit(BYTE *pPtr,USHORT usHeight);
USHORT CTOS_PrinterLogo(IN BYTE* baLogo,IN USHORT usXstart,IN USHORT usXsize,IN USHORT usY8Size);
USHORT CTOS_PrinterBufferPutString(BYTE* pbPtr, USHORT usXPos, USHORT usYPos, BYTE *baStr, CTOS_FONT_ATTRIB* ATTRIB);
USHORT CTOS_PrinterBufferOutput(BYTE* pbPtr, USHORT usY8Len);
USHORT CTOS_PrinterFline(IN USHORT usLines);
USHORT CTOS_PrinterPutString(UCHAR* baBuf);
//-------------------------------------------------------------------------

USHORT CTOS_PrinterFontSelectMode(BYTE bMode);
USHORT CTOS_PrinterTTFSelect(BYTE *baFilename, BYTE bIndex);
USHORT CTOS_PrinterTTFSwitchDisplayMode(USHORT usMode);
USHORT CTOS_PrinterStatus(void);
USHORT CTOS_PrinterBMPPic(USHORT usX, BYTE *baFilename);
USHORT CTOS_PrinterBufferFill(USHORT usXStart, USHORT usYStart, USHORT usXEnd, USHORT usYEnd, BOOL fPat);
USHORT CTOS_PrinterBufferHLine(USHORT usXStart, USHORT usYStart, USHORT usXEnd, BOOL fPat);
USHORT CTOS_PrinterBufferVLine(USHORT usXStart, USHORT usYStart, USHORT usYEnd, BOOL fPat);
USHORT CTOS_PrinterBufferLogo(USHORT usXPos, USHORT usYPos, USHORT usWidth, USHORT usHeight, BYTE *baPat);
USHORT CTOS_PrinterBufferPixel(USHORT usXPos, USHORT usYPos,BOOL fPat);
USHORT CTOS_PrinterBufferBMPPic(BYTE* pbPtr, USHORT usXPos, USHORT usYPos, BYTE *baFilename);
void CTOS_PrinterBufferEnable(void);
USHORT CTOS_PrinterBufferSelectActiveAddress (BYTE *pPtr);
USHORT CTOS_PrinterPutStringAligned (UCHAR* pbBuf, BYTE bMode);
USHORT CTOS_PrinterBufferPutStringAligned(BYTE* pbPtr,  USHORT usYPos, BYTE *baStr, CTOS_FONT_ATTRIB* ATTRIB, BYTE bMode);
USHORT CTOS_PrinterSetHeatLevel(UCHAR bHeatLevel);

//=============================================================================================================================
//
// LCD Functions
//
//=============================================================================================================================
#define d_LCD_WHITE 											  0xFFFFFF
#define d_LCD_BLACK 											  0x000000
#define d_LCD_BLUE 												  0xFF0000
#define d_LCD_GREEN 											  0x00FF00
#define d_LCD_RED 												  0x0000FF

#define d_FNT_LANGUAGE_SET 		0
#define d_FNT_ASCII_SET 		1

#define d_FNT_ID_CHECK			0
#define d_FNT_ID_SIZE_CHECK		1

#define d_UTF16               0
#define d_UTF32               1
#define d_UTF8				  3
//-----------------------------------
#define d_FNT_MODE 			0
#define d_TTF_NORM_MODE 	1
#define d_TTF_HB_MODE 		2
//-----------------------------------
#define d_LANGUAGE_DEFAULT    0
#define d_LANGUAGE_KHMER 	  1
#define d_LANGUAGE_ARABIC 	  2
#define d_LANGUAGE_THAI 	  3

#define d_FNT_ID_CHECK			0
#define d_FNT_ID_SIZE_CHECK		1

#define d_FNT_LANGUAGE_SET 		0
#define d_FNT_ASCII_SET 		1

// for show pic mode
#define d_LCD_SHOWPIC_MONO  									  0
#define d_LCD_SHOWPIC_RGB  										  1

#define d_LCD_ALIGNLEFT											  0
#define d_LCD_ALIGNCENTER										  1
#define d_LCD_ALIGNRIGHT										  2

// value for bMode
#define d_LCD_GRAPHIC_MODE										  0
#define d_LCD_TEXT_MODE											  1
#define d_LCD_GRAPHIC_HIGH_RES_MODE						  		  3
#define d_LCD_TEXT_HIGH_RES_MODE						  		  4

//for LCD attribute
#define d_RESOLUTION_720x1280		  ((720 << 16) | 1280)
#define d_RESOLUTION_480x320		  ((480 << 16) | 320)
#define d_RESOLUTION_320x480          ((320 << 16) | 480)
#define d_RESOLUTION_320x240          ((320 << 16) | 240)
#define d_RESOLUTION_128x64			  ((128 << 16) | 64)
#define d_RESOLUTION_128x32			  ((128 << 16) | 32)
#define d_COLOR_MONO  			                                  1
#define d_COLOR_262K       		                                  2
#define d_COLOR_16M        		                                  3
#define d_TOUCH_NONE	   		                                  0
#define d_TOUCH_RESISTOR   		                                  1
#define d_TOUCH_CAPACITOR_1P	                                  2


// value for English Size
#define d_LCD_FONT_8x8											  d_FONT_8x8
#define d_LCD_FONT_8x16											  d_FONT_8x16
#define d_LCD_FONT_12x24										  d_FONT_12x24

// value for bFill
#define d_LCD_FILL_0											  0
#define d_LCD_FILL_1											  1
#define d_LCD_FILL_XOR											  2

// font device definition
#define d_FONT_DEVICE_LCD_0													0x00
#define d_FONT_DEVICE_LCD_1													0x01
#define d_FONT_DEVICE_PRINTER												0x80

#define d_LCD_DEVICE_0													d_FONT_DEVICE_LCD_0
#define d_LCD_DEVICE_1													d_FONT_DEVICE_LCD_1
// Font size
#define d_FONT_8x8												  0x0808
#define d_FONT_8x16												  0x0810
#define d_FONT_16x16											  0x1010
#define d_FONT_12x24											  0x0C18
#define d_FONT_24x24											  0x1818
#define d_FONT_9x9												  0x0909
#define d_FONT_9x18												  0x0912
#define d_FONT_16x30											  0x101E
#define d_FONT_20x40											  0x1428
//--------------------Language-----------------------------------------
//----------------------------------------------------------------------
// FONT Definition
//----------------------------------------------------------------------
// font device definition
#define d_FONT_DEVICE_LCD_0													0x00
#define d_FONT_DEVICE_LCD_1													0x01
#define d_FONT_DEVICE_PRINTER												0x80

// font style definition
#define d_FONT_STYLE_NORMAL													0x0000
#define d_FONT_STYLE_REVERSE												0x0001
#define d_FONT_STYLE_UNDERLINE												0x0002
#define d_FONT_STYLE_BOLD													0x0004
#define d_FONT_STYLE_ITALIC													0x0010

// FNT Lauguage definition
#define d_FNT_LANGUAGE_ALPHABET												0x0000
#define d_FNT_LANGUAGE_CHINESE_TRADITIONAL   								0x0001	//Chinese(Traiditional)
#define d_FNT_LANGUAGE_CHINESE_SIMPLIFIED									0x0002    //Chinese(Simplified)
#define d_FNT_LANGUAGE_JAPANESE												0x0003    //Japanese
#define d_FNT_LANGUAGE_THAI  												0x0004    //Thai
#define d_FNT_LANGUAGE_VIETNAMESE										 	0x0005    //Vietnamese
#define d_FNT_LANGUAGE_KOREAN												0x0006    //Korean
#define d_FNT_LANGUAGE_RUSSIAN											 	0x0007	//Russian
#define d_FNT_LANGUAGE_PORTUGUESE_PORTUGAL								  	d_FNT_LANGUAGE_ALPHABET
#define d_FNT_LANGUAGE_SPANISH												d_FNT_LANGUAGE_ALPHABET
#define d_FNT_LANGUAGE_TURKISH 											  	d_FNT_LANGUAGE_ALPHABET
#define d_FNT_LANGUAGE_FARSI											 	d_FNT_LANGUAGE_ALPHABET

#define d_FNT_LANGUAGE_PORTUGUESE_BRAZIL        							d_FNT_LANGUAGE_ALPHABET
#define d_FNT_LANGUAGE_CZECH												d_FNT_LANGUAGE_ALPHABET
#define d_FNT_LANGUAGE_ARABIC_QATAR											d_FNT_LANGUAGE_ALPHABET


#define d_FONT_EMPTY   											  0		  //No font
#define d_FONT_CHINESE_TAIWAN   								  1028	  //Chinese(Taiwan)
#define d_FONT_CZECH											  1029	  //Czech
#define d_FONT_JAPANESE											  1041    //Japanese
#define d_FONT_KOREAN											  1042    //Korean
#define d_FONT_PORTUGUESE_BRAZIL        						  1046    //Portuguese(Brazil)
#define d_FONT_RUSSIAN											  1049    //Russian
#define d_FONT_THAI  											  1054    //Thai
#define d_FONT_TURKISH 											  1055    //Turkish
#define d_FONT_FARSI											  1065    //Farsi
#define d_FONT_VIETNAMESE										  1066    //Vietnamese
#define d_FONT_CHINESE_PRC										  2052    //Chinese(PRC)
#define d_FONT_CHINESE_PRC1										  2054    //Simple(Song)
#define d_FONT_PORTUGUESE_PORTUGAL								  2070	  //Portuguese(Portugal)
#define d_FONT_SPANISH 											  3082    //Spanish(Spain)
#define d_FONT_ARABIC_QATAR										  16385   //Arabic(Qatar)
#define d_FONT_CHINESE_TAIWAN_PLUS 								  64507	  //Chinese(Taiwan) with Symbol
#define d_FONT_DEFAULTASCII             						  36864

// FNT FontID definition
#define d_FNT_FONTID_EMPTY   												0		//No font
#define d_FNT_FONTID_CHINESE_TAIWAN   										1028	//Chinese(Taiwan)
#define d_FNT_FONTID_CZECH													1029	//Czech
#define d_FNT_FONTID_JAPANESE												1041    //Japanese
#define d_FNT_FONTID_KOREAN													1042    //Korean
#define d_FNT_FONTID_PORTUGUESE_BRAZIL        								1046    //Portuguese(Brazil)
#define d_FNT_FONTID_RUSSIAN												1049    //Russian
#define d_FNT_FONTID_THAI  													1054    //Thai
#define d_FNT_FONTID_TURKISH 												1055    //Turkish
#define d_FNT_FONTID_FARSI													1065    //Farsi
#define d_FNT_FONTID_VIETNAMESE												1066    //Vietnamese
#define d_FNT_FONTID_CHINESE_PRC											2052    //Chinese(PRC)
#define d_FNT_FONTID_CHINESE_PRC1											2054    //Simple(Song)
#define d_FNT_FONTID_PORTUGUESE_PORTUGAL									2070	//Portuguese(Portugal)
#define d_FNT_FONTID_SPANISH 												3082    //Spanish(Spain)
#define d_FNT_FONTID_ARABIC_QATAR											16385   //Arabic(Qatar)
#define d_FNT_FONTID_CHINESE_TAIWAN_PLUS 									64507	//Chinese(Taiwan) with Symbol
#define d_FNT_FONTID_DEFAULTASCII             								36864
// for font usage
#define d_FONT_LCD_USE											  0x00
#define d_FONT_PRINTER_USE										  0x01
#define d_FONT_FNT_MODE   										  0
#define d_FONT_TTF_MODE   										  1
#define	d_FONT_DEFAULT_TTF										  "ca_default.ttf"

USHORT CTOS_LCDSelectMode(BYTE bMode);
USHORT CTOS_LCDSelectModeEx(BYTE bMode,BOOL fClear);
// USHORT CTOS_LCDSetContrast(BYTE bValue);
USHORT CTOS_LCDReflesh(void);
USHORT CTOS_LCDForeGndColor(ULONG ulColor);
USHORT CTOS_LCDBackGndColor(ULONG ulColor);
USHORT CTOS_LCDFontSelectMode(BYTE bMode);
USHORT CTOS_LCDTTFSelect(BYTE *baFilename, BYTE bIndex);
USHORT CTOS_LCDTTFCheckLanguageSupport(ULONG ulLanguage, BOOL *fSupported );
USHORT CTOS_LCDTTFSwitchDisplayMode(USHORT usMode);
USHORT CTOS_LCDGetResolution(USHORT *pusXsize, USHORT *pusYsize);
USHORT CTOS_LCDAttributeGet(ULONG *pulResolution, BYTE *pbColor, BYTE *pbTouch);

// Graphic Mode
USHORT CTOS_LCDGClearCanvas(void);
USHORT CTOS_LCDGPixel(USHORT usX,USHORT usY, BOOL boPat);
USHORT CTOS_LCDGTextOut(USHORT usX, USHORT usY, UCHAR* pusBuf, USHORT usFontSize, BOOL boReverse);
USHORT CTOS_LCDGTextOutAligned( USHORT usY, UCHAR* pusBuf, USHORT usFontSize, BOOL boReverse, BYTE bMode );
USHORT CTOS_LCDGSetBox(USHORT usX, USHORT usY, USHORT usXSize, USHORT usYSize, BYTE bFill);
USHORT CTOS_LCDGShowPic(USHORT usX, USHORT usY, BYTE* baPat, ULONG ulPatLen, USHORT usXSize);
USHORT CTOS_LCDGClearWindow(void);
USHORT CTOS_LCDGMoveWindow(USHORT usOffset);
USHORT CTOS_LCDGGetWindowOffset(void);
// USHORT CTOS_LCDGMenu(BYTE bAttribute, STR *pbaHeaderString, STR *pcaItemString);
// USHORT CTOS_LCDGMenuEx(BYTE bAttribute, STR *pbaHeaderString, STR *pcaItemString,USHORT usShowItem);
USHORT CTOS_LCDGShowPicEx(UCHAR bMode,USHORT usX, USHORT usY, BYTE* baPat, ULONG ulPatLen, USHORT usXSize);
USHORT CTOS_LCDGShowBMPPic(USHORT usX, USHORT usY, BYTE *baFilename);

// Text Mode
USHORT CTOS_LCDTClearDisplay(void);
USHORT CTOS_LCDTGotoXY(USHORT usX,USHORT usY);
USHORT CTOS_LCDTWhereX(void);
USHORT CTOS_LCDTWhereY(void);
USHORT CTOS_LCDTPrint(UCHAR* sBuf);
USHORT CTOS_LCDTPrintXY(USHORT usX, USHORT usY, UCHAR* pbBuf);
USHORT CTOS_LCDTPutch(UCHAR ch);
USHORT CTOS_LCDTPutchXY (USHORT usX, USHORT usY, UCHAR bChar);
USHORT CTOS_LCDTClear2EOL(void);
USHORT CTOS_LCDTSetReverse(BOOL boReverse);
USHORT CTOS_LCDTSelectFontSize(USHORT usFontSize);
// USHORT CTOS_LCDTSetASCIIVerticalOffset(BOOL fVDirection, BYTE bVOffect);
// USHORT CTOS_LCDTSetASCIIHorizontalOffset(BOOL fHDirection, BYTE bHOffect);
// USHORT CTOS_LCDTMenu(BYTE bAttribute, STR *pbaHeaderString, STR *pcaItemString);
USHORT CTOS_LCDTPrintAligned(USHORT usY, UCHAR* pbBuf, BYTE bMode);
USHORT CTOS_LCDTGetStringWidth(BYTE *pbBuf, USHORT *pusStrWidth);

BYTE* CTOS_GETGBBuffer(void);
#ifdef __cplusplus
}
#endif

#endif

