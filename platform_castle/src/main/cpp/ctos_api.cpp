#include <jni.h>
#include <string.h>

extern "C" {

#include "typedef.h"
#include "emv_cl.h"
#include "emvaplib.h"
#include "ctoserr.h"
#include "ctosapi.h"

#include <time.h>
#include <stdlib.h>
#include <android/log.h>

#define d_VALUE_BLOCK 8

static const char *TAG = "CTOS_API_JNI";
#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO,  TAG, fmt, ##args)
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, TAG, fmt, ##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, TAG, fmt, ##args)

void set_jbyte(JNIEnv *env, jobject *obj, jbyte val, char *name) {
    jclass obj_class = env->GetObjectClass(*obj);
    jfieldID get_id = env->GetFieldID(obj_class, name, "B");
    env->SetByteField(*obj, get_id, val);
}

void set_jint(JNIEnv *env, jobject *obj, jint val, char *name) {
    jclass obj_class = env->GetObjectClass(*obj);
    jfieldID get_id = env->GetFieldID(obj_class, name, "I");
    env->SetIntField(*obj, get_id, val);
}

void set_jbyteArray(JNIEnv *env, jobject *obj, jbyte *arr, int len, char *name) {
    int num = 0, len_c;
    len_c = len;

    jclass obj_class = env->GetObjectClass(*obj);
    jfieldID get_arr_id = env->GetFieldID(obj_class, name, "[B");
    jbyteArray get_arr_fd = (jbyteArray) env->GetObjectField(*obj, get_arr_id);
    jbyte *get_arr_val = env->GetByteArrayElements(get_arr_fd, NULL);

    for (num = 0; num < len_c; num++) {
        get_arr_val[num] = arr[num];
    }

    env->ReleaseByteArrayElements(get_arr_fd, get_arr_val, NULL);
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1RTCGetTemp(JNIEnv *env, jobject thiz, jbyte bSecond,
                                                        jbyte bMinute, jbyte bHour, jbyte bDay,
                                                        jbyte bMonth, jbyte bYear, jbyte bDoW) {
    CTOS_RTC tempRTC;
    tempRTC.bDay = bDay;
    tempRTC.bDoW = bDoW;
    tempRTC.bHour = bHour;
    tempRTC.bMinute = bMinute;
    tempRTC.bMonth = bMonth;
    tempRTC.bSecond = bSecond;
    tempRTC.bYear = bYear;
    int rtn;

    rtn = CTOS_RTCGet(&tempRTC);
    set_jbyte(env, &thiz, tempRTC.bDay, "RTC_bDay");
    set_jbyte(env, &thiz, tempRTC.bDoW, "RTC_bDoW");
    set_jbyte(env, &thiz, tempRTC.bHour, "RTC_bHour");
    set_jbyte(env, &thiz, tempRTC.bMinute, "RTC_bMinute");
    set_jbyte(env, &thiz, tempRTC.bMonth, "RTC_bMonth");
    set_jbyte(env, &thiz, tempRTC.bSecond, "RTC_bSecond");
    set_jbyte(env, &thiz, tempRTC.bYear, "RTC_bYear");

    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1RTCSetTemp(JNIEnv *env, jobject thiz, jbyte bSecond,
                                                        jbyte bMinute, jbyte bHour,
                                                        jbyte bDay, jbyte bMonth, jbyte bYear,
                                                        jbyte bDoW) {
    CTOS_RTC tempRTC;
    tempRTC.bDay = bDay;
    tempRTC.bDoW = bDoW;
    tempRTC.bHour = bHour;
    tempRTC.bMinute = bMinute;
    tempRTC.bMonth = bMonth;
    tempRTC.bSecond = bSecond;
    tempRTC.bYear = bYear;
    int rtn;

    rtn = CTOS_RTCSet(&tempRTC);

    return rtn;
}

JNIEXPORT int JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1Delay(JNIEnv *env, jobject thiz, jint ulMSec) {
    int start = (int) time(NULL);
    CTOS_Delay(ulMSec);
    int end = (int) time(NULL);
    return end - start;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1TickGet(JNIEnv *env, jobject thiz) {
    int rtn = CTOS_TickGet();
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1TimeOutSet(JNIEnv *env, jobject thiz, jbyte bTID,
                                                        jint ulMSec) {
    int rtn = CTOS_TimeOutSet(bTID, ulMSec);
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1TimeOutCheck(JNIEnv *env, jobject thiz, jbyte bTID) {
    int rtn = CTOS_TimeOutCheck(bTID);
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1MSRReadTemp(JNIEnv *env, jobject thiz) {
    USHORT usTk1Len, usTk2Len, usTk3Len;
    BYTE baTk1Buf[128], baTk2Buf[128], baTk3Buf[128];
    //BYTE baBuff[1024];
    int rtn = -1;
    usTk1Len = usTk2Len = usTk3Len = 128;
    do {
        rtn = CTOS_MSRRead(baTk1Buf, &usTk1Len, baTk2Buf, &usTk2Len, baTk3Buf, &usTk3Len);
    } while (rtn != d_OK || (usTk1Len == 0 && usTk2Len == 0 && usTk3Len == 0));
    //memset(baBuff,0x00,sizeof(baBuff));
    //sprintf(baBuff, "rtn:%d, track1:%d track2:%d track3:%d", rtn, usTk1Len, usTk2Len, usTk3Len) ;
    //set_jbyteArray(env, &thiz, baBuff, 50, "baBuff");
    set_jbyteArray(env, &thiz, (jbyte *) baTk1Buf, usTk1Len, "baTk1Buf");
    set_jbyteArray(env, &thiz, (jbyte *) baTk2Buf, usTk2Len, "baTk2Buf");
    set_jbyteArray(env, &thiz, (jbyte *) baTk3Buf, usTk3Len, "baTk3Buf");
    set_jint(env, &thiz, usTk1Len, "usTk1Len");
    set_jint(env, &thiz, usTk2Len, "usTk2Len");
    set_jint(env, &thiz, usTk3Len, "usTk3Len");
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1MSRGetLastErrTemp(JNIEnv *env, jobject thiz) {
    BYTE baTk1Err, baTk2Err, baTk3Err;
    BYTE temp[128];
    int rtn;
    rtn = CTOS_MSRGetLastErr(&baTk1Err, &baTk2Err, &baTk3Err);
    //sprintf(temp, "rtn:%d\ntrack1Err:%02X\ntrack2Err:%02X\ntrack3Err:%02X", rtn, baTk1Err, baTk2Err, baTk3Err);
    //set_jbyteArray(env, &thiz, temp, 50, "baBuff");
    set_jbyte(env, &thiz, baTk1Err, "baTk1Err");
    set_jbyte(env, &thiz, baTk2Err, "baTk2Err");
    set_jbyte(env, &thiz, baTk3Err, "baTk3Err");
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1SCStatus(JNIEnv *env, jobject thiz, jint id) {
    int rtn;
    BYTE status;
    rtn = CTOS_SCStatus(id, &status);
    //sprintf(temp, "rtn:%d\ntrack1Err:%02X\ntrack2Err:%02X\ntrack3Err:%02X", rtn, baTk1Err, baTk2Err, baTk3Err);
    //set_jbyteArray(env, &thiz, temp, 50, "baBuff");
    set_jbyte(env, &thiz, status, "status");
    return rtn;
}

JNIEXPORT void JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1SCTest(JNIEnv *env, jobject thiz, jint id) {
    int EMVrtn = -1, ISOrtn = -1, APDUrtn = -1, POffrtn = -1, warmEMVrtn = -1, warmISOrtn = -1, commonrtn = -1;
    BYTE baATR[128], bATRLen, CardType;
    BYTE baSAPDU[128], baRAPDU[128];
    USHORT bSLen, bRLen;
    BYTE temp[1024];
    memset(temp, 0x00, sizeof(temp));
    bATRLen = sizeof(baATR);

    /*不能用
    commonrtn = CTOS_SCCommonReset(id, 0, 1, 1, 0, 1, d_SC_5V, baATR, &bATRLen, &CardType);
    if (commonrtn != d_OK) {
        sprintf(temp, "common Fail rtn:%d", commonrtn);
        set_jbyteArray(env, &thiz, temp, sizeof(temp), "scTestRtn");
        return;
    }
    */
    bATRLen = sizeof(baATR);
    EMVrtn = CTOS_SCResetEMV(id, d_SC_5V, baATR, &bATRLen, &CardType);
    if (EMVrtn != d_OK) {
        sprintf(reinterpret_cast<char *const>(temp), "common rtn:%d\nEMV Fail rtn:%d", commonrtn, EMVrtn);
        set_jbyteArray(env, &thiz, (jbyte*) temp, sizeof(temp), "scTestRtn");
        return;
    }

    bATRLen = sizeof(baATR);
    ISOrtn = CTOS_SCResetISO(id, d_SC_5V, baATR, &bATRLen, &CardType);
    if (ISOrtn != d_OK) {
        sprintf(reinterpret_cast<char *>(temp), "common rtn:%d\nEMV rtn:%d\nISO Fail rtn:%d", commonrtn, EMVrtn, ISOrtn);
        set_jbyteArray(env, &thiz, (jbyte*) temp, sizeof(temp), "scTestRtn");
        return;
    }

    bATRLen = sizeof(baATR);
    warmEMVrtn = CTOS_SCWarmResetEMV(id, d_SC_5V, baATR, &bATRLen, &CardType);
    if (warmEMVrtn != d_OK) {
        sprintf((char*)temp, "common rtn:%d\nEMV rtn:%d\nISO rtn:%d\nwarm EMV Fail rtn:%d", commonrtn,
                EMVrtn, ISOrtn, warmEMVrtn);
        set_jbyteArray(env, &thiz, (jbyte*) temp, sizeof(temp), "scTestRtn");
        return;
    }

    bATRLen = sizeof(baATR);
    warmISOrtn = CTOS_SCWarmResetISO(id, d_SC_5V, baATR, &bATRLen, &CardType);
    if (warmISOrtn != d_OK) {
        sprintf((char*)temp,
                "common rtn:%d\nEMV rtn:%d\nISO rtn:%d\nwarm EMV rtn:%d\nwarm ISO Fail rtn:%d",
                commonrtn, EMVrtn, ISOrtn, warmEMVrtn, warmISOrtn);
        set_jbyteArray(env, &thiz, (jbyte*) temp, sizeof(temp), "scTestRtn");
        return;
    }

    baSAPDU[0] = 0x00; //CLA
    baSAPDU[1] = 0xB2; //INS
    baSAPDU[2] = 0x01; //P1
    baSAPDU[3] = 0x0C; //P2
    baSAPDU[4] = 0x00; //Le
    bSLen = 5;
    bRLen = sizeof(baRAPDU);
    APDUrtn = CTOS_SCSendAPDU(id, baSAPDU, bSLen, baRAPDU, &bRLen);
    if (APDUrtn != d_OK) {
        sprintf((char*)temp,
                "common rtn:%d\nEMV rtn:%d\nISO rtn:%d\nwarm EMV rtn:%d\nwarm ISO rtn:%d\nAPDU Fail rtn:%d",
                commonrtn, EMVrtn, ISOrtn, warmEMVrtn, warmISOrtn, APDUrtn);
        set_jbyteArray(env, &thiz, (jbyte*) temp, sizeof(temp), "scTestRtn");
        return;
    }

    POffrtn = CTOS_SCPowerOff(id);
    if (POffrtn != d_OK) {
        sprintf((char*)temp,
                "common rtn:%d\nEMV rtn:%d\nISO rtn:%d\nwarm EMV rtn:%d\nwarm ISO rtn:%d\nAPDU rtn:%d\nPower Off Fail rtn:%d",
                commonrtn, EMVrtn, ISOrtn, warmEMVrtn, warmISOrtn, APDUrtn, POffrtn);
        set_jbyteArray(env, &thiz, (jbyte*)temp, sizeof(temp), "scTestRtn");
        return;
    }

    sprintf((char*)temp,
            "common rtn:%d\nEMV rtn:%d\nISO rtn:%d\nwarm EMV rtn:%d\nwarm ISO rtn:%d\nAPDU rtn:%d\nPower Off rtn:%d",
            commonrtn, EMVrtn, ISOrtn, warmEMVrtn, warmISOrtn, APDUrtn, POffrtn);
    set_jbyteArray(env, &thiz, (jbyte*)temp, sizeof(temp), "scTestRtn");
    return;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1CLInit(JNIEnv *env, jobject thiz) {
    int rtn;
    rtn = CTOS_CLInit();
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1CLPowerOn(JNIEnv *env, jobject thiz) {
    int rtn;
    rtn = CTOS_CLPowerOn();
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1CLPowerOff(JNIEnv *env, jobject thiz) {
    int rtn;
    rtn = CTOS_CLPowerOff();
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1FelicaPolling(JNIEnv *env, jobject thiz) {
    UCHAR ID[20];
    UCHAR PM[20];
    int rtn;
    rtn = CTOS_FelicaPolling(ID, PM);
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1CLTypeAActiveFromIdle(JNIEnv *env, jobject thiz) {
    BYTE baATQA[2];
    BYTE baSAK;
    BYTE baCSN[10];
    BYTE CSN_LEN;
    memset(baCSN, 0, sizeof(baCSN));
    CSN_LEN = sizeof(baCSN);
    int rtn;
    rtn = CTOS_CLTypeAActiveFromIdle(0, baATQA, &baSAK, baCSN, &CSN_LEN);
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1CLRATS(JNIEnv *env, jobject thiz) {
    USHORT uiRLen;
    BYTE baATS[64];
    memset(baATS, 0, sizeof(baATS));
    uiRLen = sizeof(baATS);
    int rtn;
    rtn = CTOS_CLRATS(0, baATS, &uiRLen);
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_TypeA_1Cmd(JNIEnv *env, jobject thiz) {
    USHORT usSLen, usRLen;
    BYTE baSBuf[128], baRBuf[128];

    memset(baSBuf, 0, sizeof(baSBuf));
    memset(baRBuf, 0, sizeof(baRBuf));
    const UCHAR TypeA_APDU1[0x0D] = {
            0x00, 0xA4, 0x04, 0x00, 0x07, 0xA0, 0x00, 0x00, 0x00, 0x03, 0x00, 0x00, 0x00
    };

    usSLen = 0x0D;
    usRLen = sizeof(baRBuf);
    memcpy(baSBuf, TypeA_APDU1, usSLen);
    int rtn;
    rtn = CTOS_CLAPDU(baSBuf, usSLen, baRBuf, &usRLen);
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1REQB(JNIEnv *env, jobject thiz) {
    UCHAR baATQB[1024];
    UCHAR ATQBLen;
    memset(baATQB, 0, sizeof(baATQB));
    ATQBLen = sizeof(baATQB);
    int rtn;
    rtn = CTOS_REQB(0, baATQB, &ATQBLen);
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1WUPB(JNIEnv *env, jobject thiz) {
    UCHAR baATQB[1024];
    UCHAR ATQBLen;
    memset(baATQB, 0, sizeof(baATQB));
    ATQBLen = sizeof(baATQB);
    int rtn;
    rtn = CTOS_WUPB(0, baATQB, &ATQBLen);
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1ATTRIB(JNIEnv *env, jobject thiz) {
    UCHAR baPUPI[1024];
    UCHAR bRep;
    memset(baPUPI, 0, sizeof(baPUPI));

    CTOS_CLTypeBActive(baPUPI);

    bRep = sizeof(baPUPI);
    int rtn;
    rtn = CTOS_ATTRIB(baPUPI, &bRep);
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1HALTB(JNIEnv *env, jobject thiz) {
    UCHAR baPUPI[1024];
    memset(baPUPI, 0, sizeof(baPUPI));

    CTOS_CLTypeBActive(baPUPI);
    int rtn;
    rtn = CTOS_HALTB(baPUPI);
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1CLTypeBActive(JNIEnv *env, jobject thiz) {
    BYTE baPUPI[1024];
    memset(baPUPI, 0, sizeof(baPUPI));
    int rtn;
    rtn = CTOS_CLTypeBActive(baPUPI);
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1CLTypeBActiveEx(JNIEnv *env, jobject thiz) {
    BYTE baPUPI[1024];
    memset(baPUPI, 0, sizeof(baPUPI));
    int rtn;
    rtn = CTOS_CLTypeBActiveEx(baPUPI, 0, 1, 8, 4, 424);
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1MifareLOADKEY(JNIEnv *env, jobject thiz) {
    int rtn;
    rtn = CTOS_MifareLOADKEY((UCHAR*) "\x0F\x0F\x0F\x0F\x0F\x0F\x0F\x0F\x0F\x0F\x0F\x0F");
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1MifareAUTHEx(JNIEnv *env, jobject thiz) {
    int rtn;
    BYTE baATQA[2];
    BYTE baSAK;
    BYTE baCSN[10];
    BYTE CSN_LEN;
    memset(baCSN, 0, sizeof(baCSN));
    CSN_LEN = sizeof(baCSN);
    rtn = CTOS_CLTypeAActiveFromIdle(0, baATQA, &baSAK, baCSN, &CSN_LEN);
    if (rtn != 0)
        return -2;
    rtn = CTOS_MifareLOADKEY((UCHAR*) "\x0F\x0F\x0F\x0F\x0F\x0F\x0F\x0F\x0F\x0F\x0F\x0F");
    if (rtn != 0)
        return -1;

    rtn = CTOS_MifareAUTHEx(0x60, d_VALUE_BLOCK, baCSN, CSN_LEN);
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1MifareWRITEBLOCK1(JNIEnv *env, jobject thiz) {
    int rtn;
    rtn = CTOS_MifareWRITEBLOCK(d_VALUE_BLOCK,
                                (UCHAR*) "\xFF\xFF\xFF\xFF\xFF\xFF\xFF\xFF\xFF\xFF\xFF\xFF\xFF\xFF\xFF\xFF");
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1MifareREADBLOCK1(JNIEnv *env, jobject thiz) {
    BYTE baData[17];
    memset(baData, 0, sizeof(baData));
    int rtn, i;
    rtn = CTOS_MifareREADBLOCK(d_VALUE_BLOCK, baData);
    for (i = 0; i < 16; i++) {
        if (baData[i] != 0xFF)
            return -1;
    }
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1MifareWRITEBLOCK2(JNIEnv *env, jobject thiz) {
    int rtn;
    rtn = CTOS_MifareWRITEBLOCK(d_VALUE_BLOCK,
                                (UCHAR*) "\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00");
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1MifareREADBLOCK2(JNIEnv *env, jobject thiz) {
    BYTE baData[17];
    memset(baData, 0, sizeof(baData));
    int rtn, i;
    rtn = CTOS_MifareREADBLOCK(d_VALUE_BLOCK, baData);
    for (i = 0; i < 16; i++) {
        if (baData[i] != 0x00)
            return -1;
    }
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1PowerSourceTemp(JNIEnv *env, jobject thiz) {
    BYTE temp[1024];
    memset(temp, 0x00, sizeof(temp));
    BYTE bSrc;
    int rtn;
    rtn = CTOS_PowerSource(&bSrc);
    sprintf((char*)temp, "CTOS_PowerSource rtn:%d Src:%02X", rtn, bSrc);
    set_jbyteArray(env, &thiz, (jbyte*)temp, sizeof(temp), "pwSrc");
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1PowerModeSleep(JNIEnv *env, jobject thiz) {
    int rtn;
    rtn = CTOS_PowerMode(d_PWR_SLEEP_MODE);
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1PowerModeStandby(JNIEnv *env, jobject thiz) {
    int rtn;
    rtn = CTOS_PowerMode(d_PWR_STANDBY_MODE);
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1PowerModeReboot(JNIEnv *env, jobject thiz) {
    int rtn;
    rtn = CTOS_PowerMode(d_PWR_REBOOT);
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1PowerModePWOff(JNIEnv *env, jobject thiz) {
    int rtn;
    rtn = CTOS_PowerMode(d_PWR_POWER_OFF);
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1SystemWaitTemp(JNIEnv *env, jobject thiz, jint events, jint timeout) {
    //BYTE temp[1024];    memset(temp, 0x00, sizeof(temp));
    DWORD pdwWakeupEvent = 0;
    int rtn = CTOS_SystemWait(timeout, events, &pdwWakeupEvent);
    if (rtn == d_SYSWAIT_TIMEOUT) pdwWakeupEvent = d_SYSWAIT_TIMEOUT;
    else if (rtn) pdwWakeupEvent = 0;
    //sprintf((char*)temp, "CTOS_SystemWait rtn:%d WakeupEvent:%08X", rtn, pdwWakeupEvent);
    //set_jbyteArray(env, &thiz, (jbyte*)temp, sizeof(temp), "baBuff");
    return pdwWakeupEvent;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1PowerAwakening(JNIEnv *env, jobject thiz) {
    int rtn;

    CTOS_RTC tempRTC;
    CTOS_RTCGet(&tempRTC);
    short sec = 0, min = 0;
    if ((short) tempRTC.bSecond < 57) {
        sec = ((short) tempRTC.bSecond) + (short) 3;
        min = (short) tempRTC.bMinute;
    } else {
        min = ((short) tempRTC.bMinute) + (short) 1;
        sec = (short) ((short) tempRTC.bSecond % 60);
    }
    rtn = CTOS_PowerAwakening((short) tempRTC.bSecond, (short) tempRTC.bMinute,
                              (short) tempRTC.bHour, (short) -1, (short) -1, (short) -1);
    CTOS_PowerMode(d_PWR_STANDBY_MODE);
    return rtn;
}

JNIEXPORT void JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1SystemReset(JNIEnv *env, jobject thiz) {
    CTOS_SystemReset();
}

const BYTE baPrinterBufferLogo_Single[] = { //Width=361, Height=6*8bit
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x80, 0xC0, 0x60, 0x30, 0x18, 0x18, 0x8C, 0xC4,
        0xC6,
        0x66, 0x66, 0x22, 0x22, 0x22, 0x22, 0x22, 0x62, 0x66, 0x46, 0xC4, 0x84, 0x8C, 0x18, 0x10,
        0x30,
        0x60, 0xC0, 0xC0, 0xE0, 0x38, 0x0E, 0x07, 0x1E, 0x38, 0xE0, 0x80, 0x00, 0x00, 0x00, 0x00,
        0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0xC0, 0xF8, 0x0E, 0x03, 0x81, 0xF0, 0x1C, 0x0E, 0x03, 0xC1, 0xF0,
        0xF8,
        0x7C, 0x3C, 0x1C, 0x1E, 0x1E, 0x1E, 0xFE, 0xFC, 0xFC, 0xFC, 0xF8, 0xF0, 0xC1, 0x83, 0xCE,
        0x7C,
        0x1C, 0x07, 0x81, 0xE0, 0x70, 0x1C, 0x0E, 0x1C, 0x70, 0xC0, 0x83, 0x07, 0x1C, 0x70, 0xE0,
        0x80,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x0F, 0x7F, 0xC0, 0x00, 0x03, 0x3F, 0xF0, 0x80, 0x00, 0x0F, 0x3F,
        0x7F,
        0xFF, 0xFC, 0xFC, 0xFC, 0xFC, 0xFE, 0xF3, 0xFB, 0xFF, 0xFF, 0x7F, 0x3F, 0x0F, 0x03, 0x81,
        0xE0,
        0x38, 0x0E, 0x03, 0xC0, 0xF0, 0x1C, 0x1C, 0x38, 0xE0, 0x81, 0x03, 0x0E, 0x38, 0x70, 0xC0,
        0x03,
        0x0E, 0x1C, 0x70, 0xC0, 0x80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x80,
        0xE0,
        0xF0, 0x78, 0x18, 0x18, 0x0C, 0x0C, 0x0C, 0x0C, 0x08, 0x18, 0x38, 0x30, 0x00, 0x00, 0x00,
        0x00,
        0x80, 0x80, 0xC0, 0xC0, 0xC0, 0xC0, 0x80, 0xC0, 0xC0, 0xC0, 0x00, 0x00, 0x00, 0x00, 0x80,
        0xC0,
        0xC0, 0xC0, 0xC0, 0x80, 0x00, 0x00, 0xC0, 0xFC, 0xFC, 0xFC, 0xC0, 0xC0, 0x00, 0xFC, 0xFC,
        0xFC,
        0x00, 0x00, 0x00, 0x80, 0x80, 0xC0, 0xC0, 0xC0, 0xC0, 0xC0, 0x80, 0x80, 0x00, 0x00, 0x00,
        0x00,
        0x80, 0xC0, 0xC0, 0xC0, 0xC0, 0x80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0C,
        0x0C,
        0x0C, 0xFC, 0xFC, 0xFC, 0x0C, 0x0C, 0x00, 0x00, 0x00, 0x00, 0x80, 0xC0, 0xC0, 0xC0, 0xC0,
        0xC0,
        0x80, 0x80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x80, 0x80, 0xC0, 0xC0, 0xC0, 0xC0, 0xC0,
        0x80,
        0x00, 0x00, 0x00, 0x00, 0xFC, 0xFC, 0xFC, 0x80, 0xC0, 0xC0, 0xC0, 0x80, 0x00, 0x00, 0x00,
        0x00,
        0xC0, 0xC0, 0xC0, 0xC0, 0x80, 0xC0, 0xC0, 0xC0, 0x80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x80,
        0x80,
        0xC0, 0xC0, 0xC0, 0xC0, 0x80, 0x80, 0x00, 0x00, 0x00, 0x00, 0xFC, 0xFC, 0xFC, 0x00, 0x00,
        0x00,
        0x00, 0x80, 0xC0, 0xC0, 0xC0, 0xC0, 0xC0, 0x80, 0x80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x80,
        0xC0,
        0xC0, 0xC0, 0xC0, 0x80, 0x80, 0xC0, 0xC0, 0xC0, 0x00, 0x00, 0x40, 0xC0, 0xC0, 0x80, 0x00,
        0x00,
        0x00, 0x00, 0x80, 0xC0, 0xC0, 0x40, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x80,
        0xE0,
        0xF0, 0x78, 0x18, 0x18, 0x0C, 0x0C, 0x0C, 0x0C, 0x0C, 0x18, 0x38, 0x30, 0x00, 0x00, 0x00,
        0x00,
        0x80, 0x80, 0xC0, 0xC0, 0xC0, 0xC0, 0xC0, 0x80, 0x80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00,
        0x00, 0xFC, 0xFC, 0xFC, 0x00, 0x00, 0x00, 0x00, 0xC0, 0xC0, 0xFC, 0xFC, 0xFC, 0xC0, 0x00,
        0x00,
        0x00, 0x80, 0xC0, 0xC0, 0xC0, 0xC0, 0x80, 0x80, 0xFC, 0xFC, 0xFC, 0x00, 0x00, 0x00, 0x00,
        0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,

        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x07, 0x0E, 0x18, 0x30, 0x61, 0x63, 0xC6, 0x8C,
        0x88,
        0x98, 0x98, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x90, 0x98, 0x88, 0x8C, 0xC6, 0x63, 0x61,
        0x30,
        0x18, 0x0C, 0x07, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x03, 0x07, 0x1C, 0x70, 0xE0, 0x81,
        0x07,
        0x0E, 0x38, 0xE0, 0xC1, 0x07, 0x0E, 0x38, 0x70, 0xC0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0F,
        0x3F,
        0x7F, 0xF0, 0xC0, 0xC0, 0x80, 0x80, 0x80, 0x80, 0xC0, 0xC0, 0xE0, 0x60, 0x00, 0x00, 0x00,
        0x3F,
        0xFF, 0xFF, 0xC1, 0x80, 0x80, 0x80, 0xC1, 0xFF, 0xFF, 0xFF, 0x00, 0x00, 0x00, 0x03, 0xCF,
        0x8F,
        0x9E, 0x9C, 0xFC, 0xF8, 0x20, 0x00, 0x00, 0xFF, 0xFF, 0xFF, 0x00, 0x00, 0x00, 0xFF, 0xFF,
        0xFF,
        0x00, 0x00, 0x3E, 0x7F, 0xFF, 0xCD, 0x8C, 0x8C, 0x8C, 0x8C, 0xCF, 0xCF, 0x0E, 0x08, 0x00,
        0x03,
        0xC7, 0x8F, 0x8E, 0x9C, 0xFC, 0xF9, 0x70, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00,
        0x00, 0xFF, 0xFF, 0xFF, 0x00, 0x00, 0x00, 0x00, 0x3E, 0x7F, 0xFF, 0xCD, 0xCC, 0x8C, 0x8C,
        0x8C,
        0xCF, 0xCF, 0x0F, 0x0C, 0x00, 0x00, 0x1C, 0x7F, 0xFF, 0xE3, 0xC1, 0x80, 0x80, 0x80, 0xC1,
        0xC1,
        0x41, 0x00, 0x00, 0x00, 0xFF, 0xFF, 0xFF, 0x01, 0x00, 0x00, 0xFF, 0xFF, 0xFF, 0x00, 0x00,
        0x00,
        0xFF, 0xFF, 0xFF, 0xFF, 0x00, 0x00, 0x01, 0xFF, 0xFF, 0xFF, 0x00, 0x00, 0x08, 0x3E, 0x7F,
        0xFF,
        0xC1, 0x80, 0x80, 0x80, 0xC1, 0xFF, 0x7F, 0x3E, 0x00, 0x00, 0xFF, 0xFF, 0xFF, 0x00, 0x00,
        0x3E,
        0x7F, 0xFF, 0xC1, 0x80, 0x80, 0x80, 0xC1, 0xE3, 0x7F, 0x3F, 0x08, 0x00, 0x0C, 0x7F, 0xFF,
        0xE3,
        0xC1, 0x80, 0x80, 0x80, 0xC1, 0xFF, 0xFF, 0xFF, 0x00, 0x00, 0x00, 0x03, 0x0F, 0x3F, 0xFC,
        0xF0,
        0xF8, 0x7E, 0x1F, 0x07, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0F,
        0x3F,
        0x7F, 0xF0, 0xC0, 0xC0, 0x80, 0x80, 0x80, 0x80, 0xC0, 0xC0, 0xE0, 0x60, 0x00, 0x00, 0x08,
        0x3E,
        0x7F, 0xFF, 0xC1, 0x80, 0x80, 0x80, 0xC1, 0xFF, 0x7F, 0x3E, 0x00, 0x00, 0x80, 0xC0, 0xC0,
        0x00,
        0x00, 0x00, 0x00, 0x00, 0xC0, 0x80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00,
        0x00, 0xFF, 0xFF, 0xFF, 0x80, 0x80, 0x80, 0x00, 0x00, 0x00, 0xFF, 0xFF, 0xFF, 0x00, 0x00,
        0x1C,
        0x7F, 0xFF, 0xE1, 0xC0, 0x80, 0x80, 0xC0, 0xE1, 0xFF, 0xFF, 0xFF, 0x00, 0x00, 0x00, 0xC0,
        0xC0,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,

        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x01,
        0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x00, 0x00, 0x00, 0x00,
        0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01,
        0x00,
        0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00,
        0x00, 0x00, 0x00, 0x00, 0x01, 0x01, 0x01, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00,
        0x00, 0x00, 0x00, 0x01, 0x01, 0x00, 0x00, 0x01, 0x01, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x01,
        0x01, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x01, 0x01, 0x00, 0x00, 0x00, 0x01, 0x01,
        0x01,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x01, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00,
        0x00, 0x00, 0x01, 0x01, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00,
        0x00, 0x01, 0x01, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x01, 0x01,
        0x01,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x01, 0x01, 0x00,
        0x00,
        0x00, 0x00, 0x00, 0x00, 0x01, 0x01, 0x01, 0x00, 0x00, 0x00, 0x01, 0x01, 0x01, 0x00, 0x00,
        0x00,
        0x00, 0x01, 0x01, 0x01, 0x00, 0x00, 0x00, 0x01, 0x01, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00,
        0x00, 0x01, 0x01, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x01, 0x01, 0x00, 0x00,
        0x00,
        0x00, 0x00, 0x00, 0x01, 0x01, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x06, 0x0E,
        0x0E,
        0x0D, 0x09, 0x09, 0x08, 0x0C, 0x0F, 0x07, 0x03, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0C, 0x0F,
        0x0F,
        0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00,
        0x00, 0x00, 0x00, 0x00, 0x01, 0x01, 0x01, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00,
        0x00, 0x00, 0x00, 0x01, 0x01, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00,
        0x00,
        0x00, 0x00, 0x00, 0x07, 0x03, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00,
        0x00, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x00, 0x00, 0x00, 0x01, 0x01, 0x01, 0x00, 0x00,
        0x00,
        0x00, 0x00, 0x00, 0x01, 0x01, 0x01, 0x00, 0x00, 0x01, 0x01, 0x01, 0x00, 0x00, 0x00, 0x01,
        0x01,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
};

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1PrinterLogo(JNIEnv *env, jobject thiz) {
    int rtn;
    rtn = CTOS_PrinterLogo((BYTE *) baPrinterBufferLogo_Single, 0, 361, 6);
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1PrinterPutString(JNIEnv *env, jobject thiz) {
    int rtn;
    rtn = CTOS_PrinterPutString((UCHAR*)"put string test");
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1PrinterFline(JNIEnv *env, jobject thiz) {
    int rtn;
    rtn = CTOS_PrinterFline(5);
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1PrinterSetHeatLevel(JNIEnv *env, jobject thiz,
                                                                 int level) {
    int rtn;
    rtn = CTOS_PrinterSetHeatLevel(level);
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1PrinterPutStringAligned(JNIEnv *env, jobject thiz) {
    int rtn;
    CTOS_PrinterPutStringAligned((UCHAR*)"put string align test", d_PRINTER_ALIGNLEFT);
    CTOS_PrinterPutStringAligned((UCHAR*)"put string align test", d_PRINTER_ALIGNCENTER);
    rtn = CTOS_PrinterPutStringAligned((UCHAR*)"put string align test", d_PRINTER_ALIGNRIGHT);
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1PrinterStatus(JNIEnv *env, jobject thiz) {
    int rtn;
    rtn = CTOS_PrinterStatus();
    return rtn;
}

BYTE baTemp[PAPER_X_SIZE * 40];
CTOS_FONT_ATTRIB stFONT_ATTRIB;

JNIEXPORT void JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1PrinterBufferEnable(JNIEnv *env, jobject thiz) {
    CTOS_PrinterBufferEnable();
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1PrinterBufferInit(JNIEnv *env, jobject thiz) {
    int rtn;
    memset(baTemp, 0x00, sizeof(baTemp));

    stFONT_ATTRIB.FontSize = d_FONT_12x24; // Font Size = 12x24
    stFONT_ATTRIB.X_Zoom = 1; // The width magnifies X_Zoom diameters
    stFONT_ATTRIB.Y_Zoom = 1; // The height magnifies Y_Zoom diameters
    stFONT_ATTRIB.X_Space = 0; // The width of the space between the font with next font
    stFONT_ATTRIB.Y_Space = 0; // The Height of the space between the font with next font

    rtn = CTOS_PrinterBufferInit(baTemp, 320);
    return rtn;
}


JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1PrinterBufferPutString(JNIEnv *env, jobject thiz) {
    int rtn;
    rtn = CTOS_PrinterBufferPutString((BYTE *) baTemp, 61, 57, (UCHAR*)"buffer put string test",
                                      &stFONT_ATTRIB);
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1PrinterBufferLogo(JNIEnv *env, jobject thiz) {
    int rtn;
    rtn = CTOS_PrinterBufferLogo(0, 0, 361, 48, (BYTE*) baPrinterBufferLogo_Single);
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1PrinterBufferOutput(JNIEnv *env, jobject thiz) {
    int rtn;
    rtn = CTOS_PrinterBufferOutput((BYTE *) baTemp, 32);
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1PrinterBufferFill(JNIEnv *env, jobject thiz) {
    int rtn;
    rtn = CTOS_PrinterBufferFill(0, 0, 364, 16, 1);
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1PrinterBufferHLine(JNIEnv *env, jobject thiz) {
    int rtn;
    rtn = CTOS_PrinterBufferHLine(0, 8, 364, 1);
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1PrinterBufferVLine(JNIEnv *env, jobject thiz) {
    int rtn;
    rtn = CTOS_PrinterBufferVLine(0, 8, 32, 1);
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1PrinterBufferPixel(JNIEnv *env, jobject thiz) {
    int rtn;
    rtn = CTOS_PrinterBufferPixel(0, 0, 1);
    int x, y;
    for (y = 0; y < 8; y = y + 1) {
        for (x = 0; x < 364; x = x + 1) {
            if (x % 2 == 0)
                CTOS_PrinterBufferPixel(x, y, 1);
            else
                CTOS_PrinterBufferPixel(x, y, 0);
        }
    }
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1PrinterBufferPutStringAligned(JNIEnv *env,
                                                                           jobject thiz) {
    int rtn;
    CTOS_PrinterBufferPutStringAligned((BYTE *) baTemp, 0, (UCHAR*)"Buffer put string align test",
                                       &stFONT_ATTRIB, d_PRINTER_ALIGNLEFT);
    CTOS_PrinterBufferPutStringAligned((BYTE *) baTemp, 24, (UCHAR*)"Buffer put string align test",
                                       &stFONT_ATTRIB, d_PRINTER_ALIGNCENTER);
    rtn = CTOS_PrinterBufferPutStringAligned((BYTE *) baTemp, 48, (UCHAR*)"Buffer put string align test",
                                             &stFONT_ATTRIB, d_PRINTER_ALIGNRIGHT);
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1PrinterBufferSelectActiveAddress(JNIEnv *env,
                                                                              jobject thiz) {
    int rtn;
    rtn = CTOS_PrinterBufferSelectActiveAddress((BYTE *) baTemp);
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1PrinterFontSelectMode(JNIEnv *env, jobject thiz) {
    int rtn;
    rtn = CTOS_PrinterFontSelectMode(d_FONT_TTF_MODE);
    CTOS_PrinterPutString((UCHAR*) "string test");
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1PrinterBMPPic(JNIEnv *env, jobject thiz) {
    int rtn;
    rtn = CTOS_PrinterBMPPic(0, (UCHAR*) "/data/384_100.bmp");
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1PrinterBufferBMPPic(JNIEnv *env, jobject thiz) {
    int rtn;
    rtn = CTOS_PrinterBufferBMPPic((BYTE *) baTemp, 0, 0, (UCHAR*) "/data/384_100.bmp");
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1PrinterTTFSelect(JNIEnv *env, jobject thiz) {
    int rtn;
    rtn = CTOS_PrinterTTFSelect((UCHAR*) "CHILLER.TTF", 0);
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1PrinterTTFSwitchDisplayMode(JNIEnv *env,
                                                                         jobject thiz) {
    int rtn;
    rtn = CTOS_PrinterTTFSwitchDisplayMode(d_TTF_MODE_KHMER);
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1PrinterSetDefaultASCIIStyle(JNIEnv *env,
                                                                         jobject thiz) {
    int rtn;
    rtn = CTOS_PrinterSetDefaultASCIIStyle(d_PRT_ASCII_BOLD);
    return rtn;
}

ULONG fileHandle = 0;
ULONG size = 0;

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1FileOpen(JNIEnv *env, jobject thiz) {
    int rtn;
    rtn = CTOS_FileOpen((BYTE *) "test.txt", d_STORAGE_FLASH, &fileHandle);
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1FileOpenAttrib(JNIEnv *env, jobject thiz) {
    int rtn;
    rtn = CTOS_FileOpenAttrib((UCHAR*) "test.txt", d_STORAGE_FLASH, &fileHandle, d_FA_PUBLIC);
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1FileClose(JNIEnv *env, jobject thiz) {
    int rtn;
    rtn = CTOS_FileClose(fileHandle);
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1FileDelete(JNIEnv *env, jobject thiz) {
    int rtn;
    rtn = CTOS_FileDelete((UCHAR*) "test.txt");
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1FileGetSizeTemp(JNIEnv *env, jobject thiz) {
    int rtn;
    rtn = CTOS_FileGetSize((UCHAR*) "test.txt", &size);
    set_jint(env, &thiz, size, "pulFileSize");
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1FileWrite(JNIEnv *env, jobject thiz) {
    int rtn;
    rtn = CTOS_FileWrite(fileHandle, (UCHAR*) "file write test string", strlen("file write test string"));
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1FileSeek(JNIEnv *env, jobject thiz) {
    int rtn;
    rtn = CTOS_FileSeek(fileHandle, 1, d_SEEK_FROM_CURRENT);
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1FileReadTemp(JNIEnv *env, jobject thiz) {
    int rtn;
    BYTE temp[1024];
    ULONG tempSize = 1024;
    memset(temp, 0x00, sizeof(temp));
    rtn = CTOS_FileRead(fileHandle, temp, &tempSize);
    set_jint(env, &thiz, tempSize, "pulFileSize");
    set_jbyteArray(env, &thiz, (jbyte*)temp, tempSize, "baBuff");
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1FileDirTemp(JNIEnv *env, jobject thiz) {
    int rtn;
    BYTE baFileBuf[256];
    ULONG pulFileSize;
    USHORT pusLen = 17;
    memset(baFileBuf, 0x00, sizeof(baFileBuf));
    rtn = CTOS_FileDir(baFileBuf, &pulFileSize, &pusLen);
    set_jint(env, &thiz, pulFileSize, "pulFileSize");
    set_jint(env, &thiz, pusLen, "pusLen");
    set_jbyteArray(env, &thiz, (jbyte*)baFileBuf, sizeof(baFileBuf), "baBuff");
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1Beep(JNIEnv *env, jobject thiz) {
    int rtn;
    rtn = CTOS_Beep();
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1Sound(JNIEnv *env, jobject thiz, jint usFreq, jint usDuration) {
    int rtn;
    rtn = CTOS_Sound(usFreq, usDuration);
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1GetSystemInfoTemp(JNIEnv *env, jobject thiz) {
    int rtn;
    BYTE baBuf[17];
    memset(baBuf, 0x00, sizeof(baBuf));
    rtn = CTOS_GetSystemInfo(30, baBuf);
    set_jbyteArray(env, &thiz, (jbyte*)baBuf, sizeof(baBuf), "baBuff");
    return rtn;
}

unsigned int wub_find_str_end_pt(unsigned char *str) {
    unsigned int i;
    i = 0;
    while (str[i] != 0x00)
        i++;
    return i;
}
unsigned char wub_hex_2_ascii(unsigned char hex) {
    if (hex <= 9)
        return hex + '0';
    else
        return hex - 10 + 'A';
}
void wub_str_append_byte_hex(unsigned char *str, unsigned char dat) {
    unsigned int i;
    i = wub_find_str_end_pt(str);
    str[i++] = wub_hex_2_ascii(dat / 16);
    str[i++] = wub_hex_2_ascii(dat % 16);;
    str[i] = 0x00;
}
unsigned int wub_hex_2_str(unsigned char *hex, unsigned char *str, unsigned int len) {
    unsigned int i;
    str[0] = 0;
    for (i = 0; i < len; i++)
        wub_str_append_byte_hex(str, hex[i]);
    return len * 2;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1RNGTemp(JNIEnv *env, jobject thiz) {
    int rtn;
    BYTE baBuf[128];
    BYTE baRNG[128];
    memset(baBuf, 0x00, sizeof(baBuf));
    memset(baRNG, 0x00, sizeof(baRNG));
    rtn = CTOS_RNG(baRNG);
    wub_hex_2_str(baRNG, baBuf, 8);
    set_jbyteArray(env, &thiz, (jbyte*)baBuf, sizeof(baBuf), "baBuff");
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1DESTemp(JNIEnv *env, jobject thiz) {
    int rtn;
    BYTE baBuf[128];
    BYTE baDES[128];
    memset(baBuf, 0x00, sizeof(baBuf));
    memset(baDES, 0x00, sizeof(baDES));
    rtn = CTOS_DES(0, (BYTE*)"\x01\x02\x03\x04\x05\x06\x07\x08", 8, (BYTE*)"\x31\x32\x33\x34\x35\x36\x37\x38", 8,
                   baDES);
    wub_hex_2_str(baDES, baBuf, 8);
    set_jbyteArray(env, &thiz, (jbyte*)baBuf, sizeof(baBuf), "baBuff");
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1DES_1CBCTemp(JNIEnv *env, jobject thiz) {
    int rtn;
    BYTE baBuf[128];
    BYTE baDES[128];
    memset(baBuf, 0x00, sizeof(baBuf));
    memset(baDES, 0x00, sizeof(baDES));
    rtn = CTOS_DES_CBC(0, (BYTE*)"\x01\x02\x03\x04\x05\x06\x07\x08\x09\x10\x11\x12\x13\x14\x15\x16", 16,
                       (BYTE*)"\x01\x02\x03\x04\x05\x06\x07\x08", (BYTE*)"\x31\x32\x33\x34\x35\x36\x37\x38", 8,
                       baDES);
    wub_hex_2_str(baDES, baBuf, 8);
    set_jbyteArray(env, &thiz, (jbyte*)baBuf, sizeof(baBuf), "baBuff");
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1AES_1ECBTemp(JNIEnv *env, jobject thiz) {
    int rtn;
    BYTE baBuf[128];
    BYTE baAES[128];
    memset(baBuf, 0x00, sizeof(baBuf));
    memset(baAES, 0x00, sizeof(baAES));
    rtn = CTOS_AES_ECB(0, (BYTE*)"\x01\x02\x03\x04\x05\x06\x07\x08\x09\x10\x11\x12\x13\x14\x15\x16", 16,
                       (BYTE*)"\x31\x32\x33\x34\x35\x36\x37\x38\x39\x40\x41\x42\x43\x44\x45\x46", 16,
                       baAES);
    wub_hex_2_str(baAES, baBuf, 16);
    set_jbyteArray(env, &thiz, (jbyte*)baBuf, sizeof(baBuf), "baBuff");
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1AES_1CBCTemp(JNIEnv *env, jobject thiz) {
    int rtn;
    BYTE baBuf[128];
    BYTE baAES[128];
    memset(baBuf, 0x00, sizeof(baBuf));
    memset(baAES, 0x00, sizeof(baAES));
    rtn = CTOS_AES_CBC(0, (BYTE*)"\x01\x02\x03\x04\x05\x06\x07\x08\x09\x10\x11\x12\x13\x14\x15\x16", 16,
                       (BYTE*)"\x01\x02\x03\x04\x05\x06\x07\x08\x09\x10\x11\x12\x13\x14\x15\x16",
                       (BYTE*)"\x31\x32\x33\x34\x35\x36\x37\x38\x39\x40\x41\x42\x43\x44\x45\x46", 16,
                       baAES);
    wub_hex_2_str(baAES, baBuf, 16);
    set_jbyteArray(env, &thiz, (jbyte*)baBuf, sizeof(baBuf), "baBuff");
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1MACTemp(JNIEnv *env, jobject thiz) {
    int rtn;
    BYTE baBuf[128];
    BYTE baMAC[128];
    memset(baBuf, 0x00, sizeof(baBuf));
    memset(baMAC, 0x00, sizeof(baMAC));
    rtn = CTOS_MAC((BYTE*)"\x01\x02\x03\x04\x05\x06\x07\x08\x09\x10\x11\x12\x13\x14\x15\x16", 16,
                   (BYTE*)"\x01\x02\x03\x04\x05\x06\x07\x08",
                   (BYTE*)"\x31\x32\x33\x34\x35\x36\x37\x38\x39\x40\x41\x42\x43\x44\x45\x46", 16, baMAC);
    wub_hex_2_str(baMAC, baBuf, 8);
    set_jbyteArray(env, &thiz, (jbyte*) baBuf, sizeof(baBuf), "baBuff");
    return rtn;
}

SHA_CTX SHA;

JNIEXPORT void JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1SHA1Init(JNIEnv *env, jobject thiz) {
    CTOS_SHA1Init(&SHA);
}

JNIEXPORT void JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1SHA1Update(JNIEnv *env, jobject thiz) {
    CTOS_SHA1Update(&SHA, (BYTE*)"\x31\x32\x33\x34\x35\x36\x37\x38", 8);
}

JNIEXPORT void JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1SHA1FinalTemp(JNIEnv *env, jobject thiz) {
    BYTE baBuf[128];
    BYTE baSHA[128];
    memset(baBuf, 0x00, sizeof(baBuf));
    memset(baSHA, 0x00, sizeof(baSHA));
    CTOS_SHA1Final(baSHA, &SHA);
    wub_hex_2_str(baSHA, baBuf, 20);
    set_jbyteArray(env, &thiz, (jbyte*)baBuf, sizeof(baBuf), "baBuff");
}

JNIEXPORT void JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1SHA1Temp(JNIEnv *env, jobject thiz) {
    BYTE baBuf[128];
    BYTE baSHA[128];
    memset(baBuf, 0x00, sizeof(baBuf));
    memset(baSHA, 0x00, sizeof(baSHA));
    CTOS_SHA1((BYTE*)"\x31\x32\x33\x34\x35\x36\x37\x38", 8, baSHA);
    wub_hex_2_str(baSHA, baBuf, 20);
    set_jbyteArray(env, &thiz, (jbyte*)baBuf, sizeof(baBuf), "baBuff");
}

SHA256_CTX SHA256;

JNIEXPORT void JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1SHA256Init(JNIEnv *env, jobject thiz) {
    CTOS_SHA256Init(&SHA256);
}

JNIEXPORT void JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1SHA256Update(JNIEnv *env, jobject thiz) {
    CTOS_SHA256Update(&SHA256, (BYTE*)"\x31\x32\x33\x34\x35\x36\x37\x38", 8);
}

JNIEXPORT void JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1SHA256FinalTemp(JNIEnv *env, jobject thiz) {
    BYTE baBuf[128];
    BYTE baSHA[128];
    memset(baBuf, 0x00, sizeof(baBuf));
    memset(baSHA, 0x00, sizeof(baSHA));
    CTOS_SHA256Final(&SHA256, baSHA);
    wub_hex_2_str(baSHA, baBuf, 32);
    set_jbyteArray(env, &thiz, (jbyte*)baBuf, sizeof(baBuf), "baBuff");
}

JNIEXPORT void JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1SHA256Temp(JNIEnv *env, jobject thiz) {
    BYTE baBuf[128];
    BYTE baSHA[128];
    memset(baBuf, 0x00, sizeof(baBuf));
    memset(baSHA, 0x00, sizeof(baSHA));
    CTOS_SHA256((BYTE*)"\x31\x32\x33\x34\x35\x36\x37\x38", 8, baSHA);
    wub_hex_2_str(baSHA, baBuf, 32);
    set_jbyteArray(env, &thiz, (jbyte*)baBuf, sizeof(baBuf), "baBuff");
}

const BYTE baModulus[] = {0xE4, 0x3C, 0x11, 0x36, 0xFB, 0x66, 0x3A, 0x75, 0xD9, 0x13,
                          0x19, 0x4C, 0x2A, 0x6F, 0x06, 0x5C, 0xC9, 0x69, 0x44, 0x16,
                          0x24, 0x89, 0x14, 0x23, 0x94, 0x90, 0x0E, 0x0D, 0xE4, 0x98,
                          0xF8, 0xCF, 0x02, 0xBC, 0xB5, 0x57, 0x8E, 0x57, 0x4D, 0xB8,
                          0x67, 0x68, 0x28, 0x90, 0x02, 0x4E, 0x98, 0xA3, 0xD4, 0x68,
                          0xBC, 0x87, 0xBC, 0xF5, 0x50, 0x19, 0xDD, 0x99, 0xF1, 0xE6,
                          0x62, 0xE8, 0xD4, 0xD3
};

const BYTE baExponent[] = {0x01, 0x00, 0x01};

#define d_ENCRYPTION_DATA "\x31\x32\x33\x34\x35\x36\x37\x38"

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1RSATemp(JNIEnv *env, jobject thiz) {
    int rtn = 0;
    BYTE baBuf[256];
    BYTE baRSA[256];
    memset(baBuf, 0x00, sizeof(baBuf));
    memcpy(&baBuf[sizeof(baModulus) - strlen(d_ENCRYPTION_DATA)], d_ENCRYPTION_DATA,
           strlen(d_ENCRYPTION_DATA));
    memset(baRSA, 0x00, sizeof(baRSA));
    rtn = CTOS_RSA((BYTE *) baModulus, sizeof(baModulus), (BYTE *) baExponent, sizeof(baExponent),
                   baBuf, baRSA);
    wub_hex_2_str(baRSA, baBuf, 64);
    set_jbyteArray(env, &thiz, (jbyte*)baBuf, sizeof(baBuf), "baBuff");
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1RSAKeyGenerateTemp(JNIEnv *env, jobject thiz) {
    int rtn = 0;
    CTOS_RSA_KEY_GEN para;
    BYTE baBuf[1024];
    BYTE m[256];
    BYTE d[256];
    BYTE e[3] = {0x01, 0x00, 0x01};
    para.Version = 0x01;
    para.bits = 2048;
    para.m_len = 256;
    para.m = m;
    para.d_len = 256;
    para.d = d;
    para.e_len = 3;
    para.e = e;
    rtn = CTOS_RSAKeyGenerate(&para);
    memset(baBuf, 0x00, sizeof(baBuf));
    wub_hex_2_str(para.m, baBuf, 128);
    set_jbyteArray(env, &thiz, (jbyte*)baBuf, sizeof(baBuf), "mBuf");
    memset(baBuf, 0x00, sizeof(baBuf));
    wub_hex_2_str(para.d, baBuf, 128);
    set_jbyteArray(env, &thiz, (jbyte*)baBuf, sizeof(baBuf), "dBuf");
    int mLen = para.m_len & 256;
    int dLen = para.d_len & 256;
    set_jint(env, &thiz, mLen, "mLen");
    set_jint(env, &thiz, dLen, "dLen");
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1KMS2Init(JNIEnv *env, jobject thiz) {
    int rtn = 0;
    rtn = CTOS_KMS2Init();
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1KMS2KeyCheck(JNIEnv *env, jobject thiz) {
    int rtn = 0;
    USHORT KeySet;
    USHORT KeyIndex;
    KeySet = 0x100A;
    KeyIndex = 0x0003;
    rtn = CTOS_KMS2KeyCheck(KeySet, KeyIndex);
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1KMS2KeyDelete(JNIEnv *env, jobject thiz) {
    int rtn = 0;
    USHORT KeySet;
    USHORT KeyIndex;
    KeySet = 0x2002;
    KeyIndex = 0x0004;
    rtn = CTOS_KMS2KeyDelete(KeySet, KeyIndex);
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1KMS2KeyCheckAll(JNIEnv *env, jobject thiz) {
    int rtn = 0;
    rtn = CTOS_KMS2KeyCheckAll();
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1KMS2KeyDeleteAll(JNIEnv *env, jobject thiz) {
    int rtn = 0;
    rtn = CTOS_KMS2KeyDeleteAll();
    return rtn;
}

#define KMS2_KEYTYPE_AES                0x03
#define KMS2_KEYATTRIBUTE_DECRYPT        0x00000010
#define KMS2_KEYPROTECTIONMODE_KPK_CBC  0x05


JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1KMS2KeyWrite(JNIEnv *env, jobject thiz) {
    int rtn = 0;
    BYTE const Key_AES_Dec_2002_0004[] = "AES_2002_0004_3F";
    USHORT KeySet;
    USHORT KeyIndex;
    CTOS_KMS2KEYWRITE_PARA para;
    USHORT ret;
    BYTE KeyData[16];
    BYTE baICV[16];
    KeySet = 0x2002;
    KeyIndex = 0x0004;

    memset(baICV, 0x00, sizeof(baICV));
    memcpy(KeyData, Key_AES_Dec_2002_0004, 16);
    memset(&para, 0x00, sizeof(CTOS_KMS2KEYWRITE_PARA));

    para.Version = 0x01;
    para.Info.KeySet = KeySet;
    para.Info.KeyIndex = KeyIndex;
    para.Info.KeyType = KMS2_KEYTYPE_AES;
    para.Info.KeyVersion = 0x01;
    para.Info.KeyAttribute = KMS2_KEYATTRIBUTE_DECRYPT;
    para.Protection.Mode = KMS2_KEYPROTECTIONMODE_KPK_CBC;
    para.Protection.CipherKeySet = 0x2001;
    para.Protection.CipherKeyIndex = 0x0005;
    para.Value.pKeyData = KeyData;
    para.Value.KeyLength = 16;
    para.Protection.AdditionalData.Length = sizeof(baICV);
    para.Protection.AdditionalData.pData = baICV;

    //rtn = CTOS_KMS2KeyWrite(&para);
    return rtn;
}

#define d_KEY_USAGE_DUKPT_IK                0x4231
#define d_KEY_USAGE_DATA_ENC                0x4430 // 'D0'
#define d_KEY_USAGE_PIN                     0x5030 // 'P0'
#define d_KEY_USAGE_TR31_KBPK               0x4B31 // 'K1'
#define d_KEY_USAGE_MAC                     0x4D30 // 'M0'
/* Mode Of Use */
#define d_MODE_OF_USE_DEC_ONLY              'D'
#define d_MODE_OF_USE_ENC_ONLY              'E'
#define d_MODE_OF_USE_GEN_ONLY              'G'
#define d_MODE_OF_USE_DERIVE_KEY            'X'

static BYTE gCMAC[500];
static BYTE gK1[500];
static BYTE gK2[500];
static BYTE gKeyENC[500];
static BYTE gKeyMAC[500];

static unsigned int HexToStr(unsigned char *hex, unsigned char *str, unsigned int len) {
    unsigned int i, j;
    unsigned char l, r;

    for (i = 0, j = 0; i < len; i++, j += 2) {
        l = hex[i] / 16;
        r = hex[i] % 16;
        str[j] = (l <= 9) ? (l + '0') : (l - 10 + 'A');
        str[j + 1] = (r <= 9) ? (r + '0') : (r - 10 + 'A');
    }

    str[j] = 0x00;

    return len * 2;
}

static void XOR(BYTE *A, BYTE *B, BYTE Len, BYTE *pResult) {
    BYTE i;

    for (i = 0; i < Len; ++i) {
        pResult[i] = A[i] ^ B[i];
    }
}

static void Array_LeftShiftOne(BYTE *Data) {
    int i;
    BYTE Carry = 0;

    for (i = 7; i >= 0; i--) {
        if (Data[i] >= 0x80) {
            Data[i] = Data[i] * 2 + Carry;
            Carry = 1;
        } else {
            Data[i] = Data[i] * 2 + Carry;
            Carry = 0;
        }
    }
}

static int fuSubKeyDerivation(BYTE *kbpk, USHORT kbpklen) {
    BYTE Data[8] = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
    BYTE R64[8] = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x1B};
    BYTE SubKey[8];

    CTOS_DES(0, kbpk, kbpklen, Data, 8, SubKey);

    if (SubKey[0] & 0x80) {
        Array_LeftShiftOne(SubKey);
        XOR(R64, SubKey, 8, gK1);
    } else {
        Array_LeftShiftOne(SubKey);
        memcpy(gK1, SubKey, 8);
    }

    memcpy(SubKey, gK1, 8);

    if (gK1[0] & 0x80) {
        Array_LeftShiftOne(SubKey);
        XOR(R64, SubKey, 8, gK2);
    } else {
        Array_LeftShiftOne(SubKey);
        memcpy(gK2, SubKey, 8);
    }

    return 0;
}

static int fuCMAC(
        BYTE *inMacKey, USHORT inMacKeyLen,
        BYTE *inMessage, USHORT inMessageLen) {
    BYTE i;
    BYTE rc;
    BYTE Message[256];
    USHORT MessageLen;
    BYTE data_block[256];
    BYTE temp[24];

    memcpy(Message, inMessage, inMessageLen);
    rc = inMessageLen % 8;

    if (rc) {
        memcpy(Message + inMessageLen, "\x80\x00\x00\x00\x00\x00\x00\x00", 8);
        memcpy(Message, inMessage, inMessageLen + 8 - rc);
        MessageLen = inMessageLen + 8 - rc;
        XOR(Message + MessageLen - 8, gK2, 8, Message + MessageLen - 8);
    } else {
        MessageLen = inMessageLen;
        XOR(Message + MessageLen - 8, gK1, 8, Message + MessageLen - 8);
    }

    // CMAC
    memset(data_block, 0x00, sizeof(data_block));
    for (i = 0; i < MessageLen; i = i + 8) {
        XOR(data_block, &Message[i], 8, temp);
        CTOS_DES(0, inMacKey, inMacKeyLen, temp, 8, data_block);
    }

    memcpy(gCMAC, data_block, MessageLen);

    return 0;
}

static int fuKeyDerivation(BYTE *KBPK, USHORT KBPKLen) {
    BYTE kbpk[24];
    BYTE kbpklen;
    BYTE inMAC[24];
    BYTE inENC[24];
    int i;

    kbpklen = KBPKLen;
    memcpy(kbpk, KBPK, kbpklen);

    if (kbpklen == 16) {
        memcpy(inENC, "\x00\x00\x00\x00\x00\x00\x00\x80", 8);
        memcpy(inMAC, "\x00\x00\x01\x00\x00\x00\x00\x80", 8);
    } else if (kbpklen == 24) {
        memcpy(inENC, "\x00\x00\x00\x00\x00\x01\x00\xC0", 8);
        memcpy(inMAC, "\x00\x00\x01\x00\x00\x01\x00\xC0", 8);
    } else {
        return -3;
    }

    fuSubKeyDerivation(kbpk, kbpklen);

    // some question.
    for (i = 0; i < kbpklen; i += 8) {
        inENC[0] += 1;
        inMAC[0] += 1;

        fuCMAC(kbpk, kbpklen, inENC, 8);
        memcpy(gKeyENC + i, gCMAC, 8);
        fuCMAC(kbpk, kbpklen, inMAC, 8);
        memcpy(gKeyMAC + i, gCMAC, 8);
    }

    return 0;
}

static int fuTR31(
        BYTE *inKBPK, USHORT inKBPKLen,
        BYTE *inHEADER, USHORT inHEADERLen,
        BYTE *inDATA, USHORT inDATALen,
        BYTE *Output, USHORT *OutputLen) {
    BYTE MessageMAC[48];
    BYTE EncKeyField[48];
    BYTE temp[512];
    USHORT templen;

    fuKeyDerivation(inKBPK, inKBPKLen);

    fuSubKeyDerivation(gKeyMAC, inKBPKLen);
    memcpy(MessageMAC, gCMAC, inKBPKLen);

    memcpy(temp, inHEADER, inHEADERLen);
    memcpy(temp + inHEADERLen, inDATA, inDATALen);
    templen = inHEADERLen + inDATALen;
    fuCMAC(gKeyMAC, inKBPKLen, temp, templen);
    memcpy(MessageMAC, gCMAC, 8);

    // 1 means encryption
    CTOS_DES_CBC(0, gKeyENC, inKBPKLen, MessageMAC, inDATA, inDATALen, EncKeyField);

    *OutputLen = inHEADERLen;
    memcpy(Output, inHEADER, inHEADERLen);
    *OutputLen += HexToStr(EncKeyField, Output + *OutputLen, inDATALen);
    *OutputLen += HexToStr(MessageMAC, Output + *OutputLen, 8);

    return 0;
}

int TR31_KEYBLOCK_ENCRYPTED(TR31_KEYBLOCK *pKeyBlock) {
    USHORT usTemp = 0;
    BYTE baTemp[16];

    BYTE KBPK[24];
    USHORT KBPKLen;
    BYTE baHeader[128];
    USHORT usHeaderlen;
    BYTE baKeyData[128];
    USHORT usKeyDatalen;

    BYTE KeyVersionNumber;

    BYTE TR31_Block[256];
    USHORT TR31_BlockLen;

    KBPKLen = pKeyBlock->Protection.CipherKeyLength;
    memcpy(KBPK, pKeyBlock->Protection.pCipherKeyData, KBPKLen);
    //-------------------------------------------------------------------------
    // init
    usHeaderlen = 0;
    // Key block version
    memcpy(baHeader + usHeaderlen, "B", 1);
    usHeaderlen++;
    // key block length default 80, the last update.
    memcpy(baHeader + usHeaderlen, "0080", 4);
    usHeaderlen += 4;
    //-------------------------------------------------------------------------
    // key block usage, Algorithm, mode of use
    switch (pKeyBlock->KeyInfo.KeyUsage) {
        case d_KEY_USAGE_DUKPT_IK:
            switch (pKeyBlock->KeyInfo.ModeofUse) {
                case d_MODE_OF_USE_DERIVE_KEY:
                    memcpy(baHeader + usHeaderlen, "B1", 2);
                    usHeaderlen += 2;
                    memcpy(baHeader + usHeaderlen, "T", 1);
                    usHeaderlen += 1;
                    memcpy(baHeader + usHeaderlen, "X", 1);
                    usHeaderlen += 1;
                    break;
                default:
                    return -1;
            }
            break;

        case d_KEY_USAGE_DATA_ENC:
            switch (pKeyBlock->KeyInfo.ModeofUse) {
                case d_MODE_OF_USE_DEC_ONLY:
                    memcpy(baHeader + usHeaderlen, "D0", 2);
                    usHeaderlen += 2;
                    memcpy(baHeader + usHeaderlen, "T", 1);
                    usHeaderlen += 1;
                    memcpy(baHeader + usHeaderlen, "D", 1);
                    usHeaderlen += 1;
                    break;
                case d_MODE_OF_USE_ENC_ONLY:
                    memcpy(baHeader + usHeaderlen, "D0", 2);
                    usHeaderlen += 2;
                    memcpy(baHeader + usHeaderlen, "T", 1);
                    usHeaderlen += 1;
                    memcpy(baHeader + usHeaderlen, "E", 1);
                    usHeaderlen += 1;
                    break;
                default:
                    return -1;
            }
            break;

        case d_KEY_USAGE_PIN:
            switch (pKeyBlock->KeyInfo.ModeofUse) {
                case d_MODE_OF_USE_ENC_ONLY:
                    memcpy(baHeader + usHeaderlen, "P0", 2);
                    usHeaderlen += 2;
                    memcpy(baHeader + usHeaderlen, "T", 1);
                    usHeaderlen += 1;
                    memcpy(baHeader + usHeaderlen, "E", 1);
                    usHeaderlen += 1;
                    break;
                default:
                    return -1;
            }
            break;

        case d_KEY_USAGE_TR31_KBPK:
            switch (pKeyBlock->KeyInfo.ModeofUse) {
                case d_MODE_OF_USE_DEC_ONLY:
                    memcpy(baHeader + usHeaderlen, "K1", 2);
                    usHeaderlen += 2;
                    memcpy(baHeader + usHeaderlen, "T", 1);
                    usHeaderlen += 1;
                    memcpy(baHeader + usHeaderlen, "D", 1);
                    usHeaderlen += 1;
                    break;
                default:
                    return -1;
            }
            break;

        case d_KEY_USAGE_MAC:
            switch (pKeyBlock->KeyInfo.ModeofUse) {
                case d_MODE_OF_USE_GEN_ONLY:
                    memcpy(baHeader + usHeaderlen, "M0", 2);
                    usHeaderlen += 2;
                    memcpy(baHeader + usHeaderlen, "T", 1);
                    usHeaderlen += 1;
                    memcpy(baHeader + usHeaderlen, "G", 1);
                    usHeaderlen += 1;
                    break;
                default:
                    return -1;
            }
            break;

        default:
            return -2;
    }
    //-------------------------------------------------------------------------
    // key version number
    KeyVersionNumber = pKeyBlock->KeyInfo.KeyVersionNumber;
    *(baHeader + usHeaderlen) = (KeyVersionNumber / 16 <= 9) ? (KeyVersionNumber / 16) + '0' :
                                (KeyVersionNumber / 16) - 10 + 'A';
    usHeaderlen++;
    *(baHeader + usHeaderlen) = (KeyVersionNumber % 16 <= 9) ? (KeyVersionNumber % 16) + '0' :
                                (KeyVersionNumber % 16) - 10 + 'A';
    usHeaderlen++;
    // exportability
    memcpy(baHeader + usHeaderlen, "N", 1);
    usHeaderlen += 1;
    // Optional Header
    if (pKeyBlock->KeyInfo.KeyUsage == d_KEY_USAGE_DUKPT_IK) {
        // Number of Block
        memcpy(baHeader + usHeaderlen, "01", 2);
        usHeaderlen += 2;
        // Reserved field
        memcpy(baHeader + usHeaderlen, "00", 2);
        usHeaderlen += 2;
        // Optional Block ID
        memcpy(baHeader + usHeaderlen, "KS", 2);
        usHeaderlen += 2;
        // Optional Block length
        memcpy(baHeader + usHeaderlen, "18", 2);
        usHeaderlen += 2;
        // Optional Block data
        //usTemp = pKeyBlock->KSN.KSNLength;
        //memcpy(baHeader + usHeaderlen, pKeyBlock->KSN.pKSN, usTemp);
        //usHeaderlen += usTemp;
        usHeaderlen += HexToStr(pKeyBlock->KSN.pKSN, baHeader + usHeaderlen,
                                pKeyBlock->KSN.KSNLength);
    } else {
        // Number of Block
        memcpy(baHeader + usHeaderlen, "00", 2);
        usHeaderlen += 2;
        // Reserved field
        memcpy(baHeader + usHeaderlen, "00", 2);
        usHeaderlen += 2;
    }
    //-------------------------------------------------------------------------
    // init
    usKeyDatalen = 0;
    // Key Data length
    usTemp = pKeyBlock->Value.KeyLength;
    usTemp *= 8;
    baKeyData[0] = usTemp / 0x100;
    baKeyData[1] = usTemp % 0x100;
    usKeyDatalen += 2;
    // key Data.
    memcpy(baKeyData + usKeyDatalen, pKeyBlock->Value.pKeyData, pKeyBlock->Value.KeyLength);
    usKeyDatalen += pKeyBlock->Value.KeyLength;
    // Padding
    usTemp = usKeyDatalen % 8;
    usTemp = 8 - usTemp;

    if (usTemp) {
        srand(time(NULL));
        for (; usTemp > 0; usTemp--) {
            *(baKeyData + usKeyDatalen) = rand() % 0x100;
            usKeyDatalen++;
        }
    }
    //-------------------------------------------------------------------------
    // key block length update.
    usTemp = usHeaderlen + usKeyDatalen * 2 + 8 * 2;
    sprintf((char *) baTemp, "%02d%02d", (usTemp / 100), (usTemp % 100));
    memcpy(baHeader + 1, baTemp, 4);

    fuTR31(KBPK, KBPKLen, baHeader, usHeaderlen, baKeyData, usKeyDatalen, TR31_Block,
           &TR31_BlockLen);

    pKeyBlock->Output.OutputLength = TR31_BlockLen;
    memcpy(pKeyBlock->Output.pOutputData, TR31_Block, TR31_BlockLen);

    return 0;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1KMS2KeyWriteByTR31(JNIEnv *env, jobject thiz) {
    int rtn = 0;

    BYTE const Key_3DES_100A_0003[] = "3DES_100A_0003_4";
    BYTE const Key_3DES_1000_0001[] = "3DES_KBPK_1000_0001_0000"; // KBPK

    TR31_KEYBLOCK TR31KeyBlock;
    CTOS_KMS2KEYWRITEBYTR31_PARA stWriteKeyTR31;
    USHORT KeySet;
    USHORT KeyIndex;

    BYTE KBPK[] = "3DES_KBPK_000020"; // Cipher Key
    BYTE baKeyData[] = "3DES_DEC_BY_TR31";
    BYTE baOutput[512];

    memset(&stWriteKeyTR31, 0x00, sizeof(stWriteKeyTR31));
    memset(&TR31KeyBlock, 0x00, sizeof(TR31KeyBlock));
    memset(baOutput, 0x00, sizeof(baOutput));

    TR31KeyBlock.KeyInfo.KeyVersionNumber = 0x01;
    TR31KeyBlock.KeyInfo.KeyUsage = d_KEY_USAGE_DATA_ENC;
    TR31KeyBlock.KeyInfo.ModeofUse = d_MODE_OF_USE_DEC_ONLY;
    TR31KeyBlock.Value.KeyLength = 16;
    TR31KeyBlock.Value.pKeyData = baKeyData;
    TR31KeyBlock.Protection.CipherKeyLength = 16;
    TR31KeyBlock.Protection.pCipherKeyData = KBPK;
    TR31KeyBlock.Output.pOutputData = baOutput;

    TR31_KEYBLOCK_ENCRYPTED(&TR31KeyBlock);

    //-------------------------------------------------------------------------
    memset(&stWriteKeyTR31, 0x00, sizeof(CTOS_KMS2KEYWRITEBYTR31_PARA));

    KeySet = 0x100A;
    KeyIndex = 0x0003;

    stWriteKeyTR31.Version = 0;
    stWriteKeyTR31.Info.KeySet = KeySet;
    stWriteKeyTR31.Info.KeyIndex = KeyIndex;
    stWriteKeyTR31.Protection.CipherKeySet = 0x1000;
    stWriteKeyTR31.Protection.CipherKeyIndex = 0x0001;
    stWriteKeyTR31.Value.KeyLength = TR31KeyBlock.Output.OutputLength;
    stWriteKeyTR31.Value.pKeyData = TR31KeyBlock.Output.pOutputData;

    rtn = CTOS_KMS2KeyWriteByTR31(&stWriteKeyTR31);
    return rtn;
}

#define KMS2_MACMETHOD_CBC  0x00

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1KMS2MAC(JNIEnv *env, jobject thiz) {
    int rtn = 0;
    CTOS_KMS2MAC_PARA macpara;
    BYTE aICV[16];
    BYTE aInData[32];
    BYTE aOutData[32];

    memset(&macpara, 0x00, sizeof(macpara));
    macpara.Version = 0x01;
    macpara.Protection.CipherKeySet = 0x2000;
    macpara.Protection.CipherKeyIndex = 0x0002;
    macpara.Protection.CipherMethod = KMS2_MACMETHOD_CBC;
    macpara.Protection.SK_Length = 0;
    macpara.ICV.Length = 0x10;
    memset(aICV, 0x00, sizeof(aICV));
    memcpy(aICV, "KMS2TESTKMS2TEST", 0x10);
    macpara.ICV.pData = aICV;
    macpara.Input.Length = 0x18;
    memset(aInData, 0x00, sizeof(aInData));
    memcpy(aInData,
           "\x34\x30\x31\x32\x33\x34\x35\x36\x37\x38\x39\x30\x39\x44\x39\x38\x37\x00\x00\x00\x00\x00\x00\x00",
           0x18);
    macpara.Input.pData = aInData;
    memset(aOutData, 0x00, sizeof(aOutData));
    macpara.Output.pData = aOutData;
    rtn = CTOS_KMS2MAC(&macpara);
    return rtn;
}

#define KMS2_DATAENCRYPTCIPHERMETHOD_CBC     0x01

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1KMS2DataEncrypt(JNIEnv *env, jobject thiz) {
    int rtn = 0;
    CTOS_KMS2DATAENCRYPT_PARA dataencryptpara;
    BYTE aInData[48];
    BYTE aOutData[48];
    BYTE baICV[16] = {0};
    BYTE key;
    BYTE str[20];

    memset(&dataencryptpara, 0x00, sizeof(dataencryptpara));
    dataencryptpara.Version = 0x01;
    dataencryptpara.Protection.CipherKeySet = 0x2000;
    dataencryptpara.Protection.CipherKeyIndex = 0x0003;
    dataencryptpara.Protection.CipherMethod = KMS2_DATAENCRYPTCIPHERMETHOD_CBC;
    dataencryptpara.Protection.SK_Length = 0;
    dataencryptpara.Input.Length = 0x10;
    dataencryptpara.Input.pICV = baICV;
    dataencryptpara.Input.ICVLength = 16;
    memset(aInData, 0x00, sizeof(aInData));
    memcpy(aInData, "0123456789ABCDEF", 0x10);
    dataencryptpara.Input.pData = aInData;
    memset(aOutData, 0x00, sizeof(aOutData));
    dataencryptpara.Output.pData = aOutData;
    rtn = CTOS_KMS2DataEncrypt(&dataencryptpara);
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1KMS2KeySwap(JNIEnv *env, jobject thiz) {
    int rtn = 0;
    CTOS_KMS2KEYSWAP_PARA stPara;

    memset(&stPara, 0x00, sizeof(stPara));

    stPara.Version = 0x01;

    stPara.Source1.KeySet = 0x01;
    stPara.Source1.KeyIndex = 0x02;

    stPara.Source2.KeySet = 0x03;
    stPara.Source2.KeyIndex = 0x04;

    rtn = CTOS_KMS2KeySwap(&stPara);
    return rtn;
}


JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1LanguageConfig(JNIEnv *env, jobject thiz) {
    int rtn = 0;
    rtn = CTOS_LanguageConfig(d_FONT_CHINESE_TAIWAN, d_FONT_16x16, 0, FALSE);
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1LanguageLCDFontSize(JNIEnv *env, jobject thiz) {
    int rtn = 0;
    //rtn = CTOS_LanguageLCDFontSize(d_FONT_20x40,0);
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1LanguagePrinterFontSize(JNIEnv *env, jobject thiz) {
    int rtn = 0;
    rtn = CTOS_LanguagePrinterFontSize(d_FONT_20x40, 0, d_FONT_NO_SET_TYPE);
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1LanguageInfoTemp(JNIEnv *env, jobject thiz) {
    int rtn = 0;
    USHORT pusLanguage = 0, pusFontSize = 0, pusFontStyle = 0;
    BYTE baBuf[1024];
    memset(baBuf, 0x00, sizeof(baBuf));
    rtn = CTOS_LanguageInfo(1, &pusLanguage, &pusFontSize, &pusFontStyle);
    sprintf((char*)baBuf, "pusLanguage:%d\npusFontSize:%d\npusFontStyle:%d\n", pusLanguage, pusFontSize,
            pusFontStyle);
    set_jbyteArray(env, &thiz, (jbyte*)baBuf, sizeof(baBuf), "baBuff");
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1LanguageNumTemp(JNIEnv *env, jobject thiz) {
    int rtn = 0;
    USHORT pusIndex = 0;
    BYTE baBuf[1024];
    memset(baBuf, 0x00, sizeof(baBuf));
    rtn = CTOS_LanguageNum(&pusIndex);
    sprintf((char*)baBuf, "pusIndex:%d\n", pusIndex);
    set_jbyteArray(env, &thiz, (jbyte*)baBuf, sizeof(baBuf), "baBuff");
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1LanguagePrinterSelectASCII(JNIEnv *env, jobject thiz) {
    int rtn = 0;
    rtn = CTOS_LanguagePrinterSelectASCII(1);
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1LanguageLCDSelectASCII(JNIEnv *env, jobject thiz) {
    int rtn = 0;
    //rtn = CTOS_LanguageLCDSelectASCII(1);
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1LanguagePrinterGetFontInfoTemp(JNIEnv *env,
                                                                            jobject thiz) {
    int rtn = 0;
    USHORT usASCIIFontID = 0, usFontSize = 0, usFontStyle = 0;
    BYTE baBuf[1024];
    memset(baBuf, 0x00, sizeof(baBuf));
    rtn = CTOS_LanguagePrinterGetFontInfo(&usASCIIFontID, &usFontSize, &usFontStyle);
    sprintf((char*)baBuf, "usASCIIFontID:%d\nusFontSize:%d\nusFontStyle:%d\n", usASCIIFontID, usFontSize,
            usFontStyle);
    set_jbyteArray(env, &thiz, (jbyte*)baBuf, sizeof(baBuf), "baBuff");
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1LanguageLCDGetFontInfoTemp(JNIEnv *env, jobject thiz) {
    int rtn = 0;
    int usASCIIFontID = 0, usFontSize = 0, usFontStyle = 0;
    BYTE baBuf[1024];
    memset(baBuf, 0x00, sizeof(baBuf));
    //rtn = CTOS_LanguageLCDGetFontInfo(&usASCIIFontID,&usFontSize,&usFontStyle);
    sprintf((char*)baBuf, "usASCIIFontID:%d\nusFontSize:%d\nusFontStyle:%d\n", usASCIIFontID, usFontSize,
            usFontStyle);
    set_jbyteArray(env, &thiz, (jbyte*)baBuf, sizeof(baBuf), "baBuff");
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1LCDSelectMode(JNIEnv *env, jobject thiz) {
    int rtn = 0;
    rtn = CTOS_LCDSelectMode(d_LCD_TEXT_MODE);
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1LCDTClearDisplay(JNIEnv *env, jobject thiz) {
    int rtn = 0;
    rtn = CTOS_LCDTClearDisplay();
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1LCDTGotoXY(JNIEnv *env, jobject thiz) {
    int rtn = 0;
    rtn = CTOS_LCDTGotoXY(10, 8);
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1LCDTWhereX(JNIEnv *env, jobject thiz) {
    int rtn = 0;
    rtn = CTOS_LCDTWhereX();
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1LCDTWhereY(JNIEnv *env, jobject thiz) {
    int rtn = 0;
    rtn = CTOS_LCDTWhereY();
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1LCDTPrint(JNIEnv *env, jobject thiz) {
    int rtn = 0;
    rtn = CTOS_LCDTPrint((UCHAR*)"hello!");
    return rtn;
}

JNIEXPORT jint JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1LCDTPrintXY(JNIEnv *env, jobject thiz) {
    int rtn = 0;
    rtn = CTOS_LCDTPrintXY(2, 6, (UCHAR*)"hello!");
    return rtn;
}

JNIEXPORT jbyteArray JNICALL
Java_com_blk_platform_1castle_CTOS_1API_CTOS_1GETGBBuffer(JNIEnv *env, jobject thiz) {
    int size = 720 * 1280 * 4 + 54;
    BYTE *buffer = NULL;
    buffer = CTOS_GETGBBuffer();
    if (buffer == NULL) {
        return nullptr;
    }
    jbyteArray array;
    array = env->NewByteArray(size);
    env->SetByteArrayRegion(array, 0, size, (jbyte *) buffer);
    return array;
}

}