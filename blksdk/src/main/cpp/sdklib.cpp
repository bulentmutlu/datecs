#include <jni.h>
#include <android/log.h>

// Write C++ code here.
//
// Do not forget to dynamically load the C++ library into your application.
//
// For instance,
//
// In MainActivity.java:
//    static {
//       ISystem.loadLibrary("techpos");
//    }
//
// Or, in MainActivity.kt:
//    companion object {
//      init {
//         ISystem.loadLibrary("techpos")
//      }
//    }

#ifdef __cplusplus
extern "C" {
#endif

#include "iso8583/dl_iso8583.h"
#include "iso8583/dl_iso8583_defs_1993.h"
#include "iso8583/dl_output.h"

void DumpBerTLV(const char *caption, const unsigned char *tlv, int len);
void EP_BfAscii(char *Out, unsigned char *In, int InLen)
{
    int i;
    unsigned char TempByte;

    for (i = 0; i < InLen; i++) {
        TempByte = ((In[i] & 0xF0) >> 4);
        *Out = 0x30 + TempByte;
        if ((TempByte >= 0x0A) && (TempByte <= 0x0F))
            *Out += 0x07;
        Out++;
        TempByte = (In[i] & 0x0F);
        *Out = 0x30 + TempByte;
        if ((TempByte >= 0x0A) && (TempByte <= 0x0F))
            *Out += 0x07;
        Out++;
    }
    return;
}

#ifdef __cplusplus
}
#endif

#include <string>
#include <stdlib.h>
#include <vector>
#include <map>
typedef unsigned char byte;

std::map<int, std::string> Dump (const DL_ISO8583_HANDLER *iHandler,
                          const DL_ISO8583_MSG     *iMsg )
{
    std::map<int, std::string> m;

    DL_UINT16 i;

    /* for each field */
    for ( i=0 ; i<(iHandler->fieldItems) ; i++ )
    {
        if ( NULL != iMsg->field[i].ptr ) /* present */
        {
            DL_ISO8583_FIELD_DEF *fieldDef = DL_ISO8583_GetFieldDef(i,iHandler);

            if (fieldDef->fieldType == kDL_ISO8583_B || fieldDef->len == 999) // fieldDef->varLen > 0
            {
                int dumpLen = iMsg->field[i].len; //(*DebugModuls() & DM_VERBOSE) ? iMsg->field[i].len : MIN(100, iMsg->field[i].len);
                char *pTmp = (char *) malloc(dumpLen * 2 + 1);
                if (pTmp == NULL) continue;

                void EP_BfAscii(char *Out, unsigned char *In, int InLen);
                EP_BfAscii(pTmp, (unsigned char *) iMsg->field[i].ptr, dumpLen);

                pTmp[dumpLen * 2] = 0;

                //idebug(DM_ISO, "[%02d] [%03d] %s\n", (int)i, iMsg->field[i].len, pTmp);
                __android_log_print(ANDROID_LOG_INFO, "com.blk.techpos", "[%02d] [%03d] %s", (int)i, iMsg->field[i].len, pTmp);
                m.emplace(i, pTmp);
                free(pTmp);

                //void DumpTLV(ULONG module, const unsigned char *tlv, int len);
                //DumpTLV(DM_ISO, iMsg->field[i].ptr, iMsg->field[i].len);
            }
            else {
                //idebug(DM_ISO, "[%02d] [%03d] %s\n", (int) i, iMsg->field[i].len, iMsg->field[i].ptr);
                __android_log_print(ANDROID_LOG_INFO, "com.blk.techpos", "[%02d] [%03d] %s\n", (int) i, iMsg->field[i].len, iMsg->field[i].ptr);
                m.emplace(i, std::string((char *) iMsg->field[i].ptr, iMsg->field[i].len));
            }

        }

    } /* end-for(i) */

    __android_log_print(ANDROID_LOG_INFO, "com.blk.techpos", "----------------------------------------------------------------------------------------------------");
    //idebug(DM_ISO, "----------------------------------------------------------------------------------------------------\n");

    return std::move(m);
}


// JNI Helpers


static jstring ToJString(JNIEnv *env, const char *str)
{
    return env->NewStringUTF(str);
}
static char* ToCString(JNIEnv *env, jstring str)
{
    const char *inCStr = env->GetStringUTFChars(str, NULL);
    int len = (NULL == inCStr) ? 0 : strlen(inCStr);

    static char *outStr;
    if (outStr) free(outStr);

    outStr = (char *)calloc(len + 1, sizeof (char));

    if (inCStr) strcpy(outStr, inCStr);

    env->ReleaseStringUTFChars(str, inCStr);  // release resources

    return outStr;
}
static std::vector<byte> ToByteVector(JNIEnv *env, jbyteArray buf, jint length)
{
	std::vector<byte> vec(length);
	env->GetByteArrayRegion(buf, 0, length, (jbyte*)vec.data());
	return std::move(vec);
}
static std::vector<byte> ToByteVector(JNIEnv *env, jbyteArray buf)
{
    return ToByteVector(env, buf, env->GetArrayLength(buf));
}
static jbyteArray ToByteArray(JNIEnv *env, const std::vector<byte> &vec)
{
	int size = (int) vec.size();
	jbyteArray output = env->NewByteArray(size);
	env->SetByteArrayRegion(output, 0, size, (jbyte*)vec.data());
	return output;
}
static jbyteArray ToByteArray(JNIEnv *env, const byte *cArray, int length)
{
    jbyteArray output = env->NewByteArray(length);
    env->SetByteArrayRegion(output, 0, length, (jbyte*)cArray);
    return output;
}
static jobject ToHashMap(JNIEnv *env, const std::map<int, std::string> &m)
{
    env->PushLocalFrame(1024); // fix for local references

    jclass hashMapClass= env->FindClass("java/util/HashMap");
    jmethodID hashMapInit = env->GetMethodID(hashMapClass, "<init>", "(I)V");
    jmethodID hashMapOut = env->GetMethodID(hashMapClass, "put",
                                            "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");

    jclass IntegerClass = env->FindClass("java/lang/Integer");
    jmethodID intInit = env->GetMethodID(IntegerClass, "<init>", "(I)V");

    jobject hashMapObj = env->NewObject(hashMapClass, hashMapInit, (jint) m.size());

    for (auto it : m)
    {
        env->CallObjectMethod(hashMapObj, hashMapOut,
                              env->NewObject(IntegerClass, intInit, (jint) it.first),
                              env->NewStringUTF(it.second.c_str()));
    }

    env->PopLocalFrame(hashMapObj);

    return hashMapObj;
}

// // JNI Helpers
typedef struct  {
    DL_ISO8583_HANDLER isoHandler;
    DL_ISO8583_MSG     isoMsg;
} iso8583;

extern "C"
JNIEXPORT jlong JNICALL
Java_com_blk_sdk_Iso8583_newHandle(JNIEnv *env, jclass clazz) {

    iso8583 *p = new iso8583();
    DL_ISO8583_DEFS_1993_GetHandler(&p->isoHandler);
    DL_ISO8583_MSG_Init(NULL,0,&p->isoMsg);
    return (jlong) p;
}
extern "C"
JNIEXPORT void JNICALL
Java_com_blk_sdk_Iso8583_freeHandle(JNIEnv *env, jclass clazz, jlong handle) {
    // TODO: implement FreeHandle()
    iso8583 *p = (iso8583 *) handle;
    DL_ISO8583_MSG_Free(&p->isoMsg);
    delete p;
}
extern "C"
JNIEXPORT void JNICALL
Java_com_blk_sdk_Iso8583_setField(JNIEnv *env, jobject thiz, jlong handle, jint field,
                                  jstring value) {
    iso8583 *p = (iso8583 *) handle;
    DL_ISO8583_MSG_SetField_Str(field,(DL_UINT8*) ToCString(env, value),&p->isoMsg);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_blk_sdk_Iso8583_setFieldBin(JNIEnv *env, jobject thiz, jlong handle, jint field,
                                     jbyteArray value, jint length) {
    iso8583 *p = (iso8583 *) handle;
    std::vector<byte> data = ToByteVector(env, value, length);
    DL_ISO8583_MSG_SetField_Bin(field,(DL_UINT8*) data.data(), length, &p->isoMsg);
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_blk_sdk_Iso8583_getFieldString(JNIEnv *env, jobject thiz, jlong handle,
                                        jint field) {

    DL_UINT8 *tmpStr = NULL;
    iso8583 *p = (iso8583 *) handle;
    if(DL_ISO8583_MSG_GetField_Str(field, &p->isoMsg, &tmpStr))
        return nullptr;
    return ToByteArray(env, (byte*) tmpStr, std::string((char*)tmpStr).size());
    //ToJString(env, (char*) tmpStr);
}
extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_blk_sdk_Iso8583_getFieldBin(JNIEnv *env, jobject thiz, jlong handle, jint field) {

    iso8583 *p = (iso8583 *) handle;
    DL_UINT8             *oPtr = NULL;
    DL_UINT16             oByteLen;
    if(DL_ISO8583_MSG_GetField_Bin(field, &p->isoMsg, &oPtr, &oByteLen))
       return nullptr;
    return ToByteArray(env, (byte*) oPtr, oByteLen);
}
extern "C"
JNIEXPORT jobject JNICALL
Java_com_blk_sdk_Iso8583_dump(JNIEnv *env, jobject thiz, jlong handle) {
    iso8583 *p = (iso8583 *) handle;

    //return ToHashMap(env, Dump(&p->isoHandler, &p->isoMsg));
    Dump(&p->isoHandler, &p->isoMsg);
    return nullptr;
    //DL_ISO8583_MSG_Dump(String(MessageTypeString(MsgType)).Append("  RESPONSE ISO8583 MSG ").Pad(100, '-').Append("\n").c_str(), &isoHandler,&isoMsg);

}
extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_blk_sdk_Iso8583_pack(JNIEnv *env, jobject thiz, jlong handle) {

    iso8583 *p = (iso8583 *) handle;

    DL_UINT8                 ioByteArr[2048];
    DL_UINT16                oNumBytes;
    int err = DL_ISO8583_MSG_Pack(&p->isoHandler,&p->isoMsg,ioByteArr, &oNumBytes);

    if (err) return nullptr;

    return ToByteArray(env, (byte *) ioByteArr, oNumBytes);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_blk_sdk_Iso8583_unpack(JNIEnv *env, jclass thiz, jlong handle,
                                jbyteArray iso_data, jint length) {

    iso8583 *p = (iso8583 *) handle;

    auto isoData = ToByteVector(env, iso_data, length);
    DL_ISO8583_MSG_Unpack(&p->isoHandler, (DL_UINT8 *) isoData.data(), (DL_UINT16) length,&p->isoMsg);
}

//
//
//static int EncryptWithHPK(const unsigned char *in, int len, char *out)
//{
//    char tIn[128]={0}, tOut[128]={0};
//    int rv = 0;
////#ifdef _DEBUG
//    unsigned char tmpHkp[200]={0};
//
////#endif
//
//    memcpy(tmpHkp, "\xE0\x22\x18\xCE\x4B\x96\x4B\xFB\x29\x16\x80\xB8\xA1\x04\xA9\x38" \
//                    "\x10\x78\x59\x16\xF2\x81\x5B\x9F\x61\x92\x7E\x61\x01\x8F\x23\x1C" \
//                    "\xE7\xED\xC7\x25\x58\xC0\xAD\x4B\x54\xF6\x48\xF2\xA8\x26\x66\xAD" \
//                    "\x2A\xC8\x6C\xAD\x34\x60\x08\xEE\xDE\xAA\x02\x4A\x0C\x51\x02\x11" \
//                    "\x7C\xCC\x0F\x53\xA8\x3C\xCB\x3C\x8C\xEE\x46\xB5\x4D\x62\x8D\x00" \
//                    "\x21\x94\xEB\x46\x0A\x82\xF8\xA5\xE9\x2D\xC8\x75\x89\xFB\x53\xE1" \
//                    "\xF5\xE2\xDF\x45\x6C\xCC\x60\x9A\x6C\x75\x54\xF0\x97\xF4\xEB\x41" \
//                    "\x19\xF9\x62\x55\xF9\x5D\xB8\x7F\xEB\x32\xCB\x44\x44\x39\x28\xAB", 128);
//
//
//    idebug(DM_BKM|DM_VERBOSE, "Encrypt with HPK (RSA) start..HPK KEY:\n");
//    //EP_HexDump(tmpHkp, 128);
////    EP_HexDump(params->HPK, 128);
//
//    memset(tIn, 0, sizeof(tIn));
//    memset(tOut, 0, sizeof(tOut));
//
//    RSA * pubkey = RSA_new();
//
//    BIGNUM * modul = BN_bin2bn(tmpHkp /*params->HPK*/, 128, NULL);
//    BIGNUM * expon = BN_bin2bn("\x01\x00\x01", 3, NULL);
//
//    pubkey->n = modul;
//    pubkey->e = expon;
//
//    memcpy(tIn, in, len);
//
//    rv = RSA_public_encrypt(len, in, out, pubkey, RSA_PKCS1_PADDING );
//    if (rv <= 0)
//    {
//        idebug(DM_BKM, "ERROR encrypt!\n");
//        rv = -1;
//    }
//
//    RSA_free(pubkey);
//
//    iHexDump("Encrypt with HPK", out, len, DM_BKM|DM_VERBOSE);
//    return rv;
//}

extern "C" {
#include "../../../../jniLibs/openssl/armeabi-v7a/include/openssl/rsa.h"
#include "../../../../jniLibs/openssl/armeabi-v7a/include/openssl/bn.h"
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_blk_sdk_Openssl_EncryptWithHPK(JNIEnv *env, jclass clazz, jbyteArray key,
                                        jbyteArray in, jint len) {

    RSA * pubkey = RSA_new();
    byte out[128], tin[128];
    memcpy(tin, ToByteVector(env, in).data(), len);

    BIGNUM * modul = BN_bin2bn(ToByteVector(env, key).data(), 128, NULL);
    BIGNUM * expon = BN_bin2bn((byte*)"\x01\x00\x01", 3, NULL);
    pubkey->n = modul;
    pubkey->e = expon;

    int outlen = RSA_public_encrypt(len, tin, out, pubkey, RSA_PKCS1_PADDING );
    RSA_free(pubkey);
    if (outlen <= 0)
    {
        //idebug(DM_BKM, "ERROR encrypt!\n");
        return nullptr;
    }
    return ToByteArray(env, out, outlen);
}

#include "../../../../jniLibs/openssl/armeabi-v7a/include/openssl/des.h"

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_blk_sdk_Openssl_DecryptMsg(JNIEnv *env, jclass clazz, jbyteArray key,
                                    jbyteArray in, jint len) {

    if (len % 8 != 0) {
        __android_log_print(ANDROID_LOG_FATAL, "sdklib", "Openssl_DecryptMsg len(%d) len%8(%d)", len, len%8);
    }

    int i = 0;
    byte *out = (byte *)malloc(len);

    DES_cblock cb1, cb2;
    DES_key_schedule ks1, ks2;
    DES_cblock cblock = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };

    __android_log_print(ANDROID_LOG_INFO, "sdklib", "Openssl_DecryptMsg key(%d) in(%d) len(%d)",
                        env->GetArrayLength(key), env->GetArrayLength(in), len);

    std::vector<byte> vKey = ToByteVector(env, key);
    std::vector<byte> vIn = ToByteVector(env, in, len);

    memcpy(cb1, vKey.data(), 8);
    memcpy(cb2, vKey.data() + 8, 8);

    //iHexDump("MSG DECRIPT CB1 MSK", cb1, 8, DM_BKM|DM_VERBOSE);
    //iHexDump("MSG DECRIPT CB2 MSK", cb2, 8, DM_BKM|DM_VERBOSE);


    DES_set_key(&cb1, &ks1);
    DES_set_key(&cb2, &ks2);

    DES_ede3_cbc_encrypt(vIn.data(), out, len, &ks1, &ks2, &ks1, &cblock, DES_DECRYPT);

    jbyteArray rv = ToByteArray(env, out, len);
    free(out);

    return rv;
}
extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_blk_sdk_Openssl_EncryptMsg(JNIEnv *env, jclass clazz, jbyteArray key,
                                    jbyteArray in, jint len) {

    int i = 0;
    unsigned char padByte = 0;
    byte *out = (byte *)malloc(len + 8);

    DES_cblock cb1, cb2;
    DES_key_schedule ks1, ks2;
    DES_cblock cblock = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };

    std::vector<byte> vKey = ToByteVector(env, key);
    std::vector<byte> vIn = ToByteVector(env, in, len);

    memcpy(cb1, vKey.data(), 8);
    memcpy(cb2, vKey.data() + 8, 8);

    DES_set_key(&cb1, &ks1);
    DES_set_key(&cb2, &ks2);

    padByte = (unsigned char)(8 - (len % 8));
    for(i = 0; i < padByte; i++)
    {
        //in[len++] = padByte;
        vIn.push_back(padByte);
        ++len;
    }

    DES_ede3_cbc_encrypt(vIn.data(), out, len, &ks1, &ks2, &ks1, &cblock, DES_ENCRYPT);

    //memcpy(in, out, len);
    jbyteArray rv = ToByteArray(env, out, len);

    free(out);

    return rv;
}
extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_blk_sdk_Openssl_Des(JNIEnv *env, jclass clazz, jboolean fEncrypt, jbyteArray key, jbyteArray in) {
    unsigned char out[8];

    std::vector<byte> vKey = ToByteVector(env, key);
    std::vector<byte> vIn = ToByteVector(env, in, 8);

    DES_cblock dkey, dkey2;
    DES_key_schedule keysched, keysched2;

    DES_set_key((C_Block *)vKey.data(), &keysched);
    DES_set_key((C_Block *)vKey.data() + 8, &keysched2);

    DES_ecb_encrypt((C_Block *)vIn.data(),(C_Block *)out, &keysched, fEncrypt ? DES_ENCRYPT : DES_DECRYPT);
    //DES_ecb3_encrypt((C_Block *)vIn.data(),(C_Block *)out, &keysched, &keysched2, &keysched, fEncrypt ? DES_ENCRYPT : DES_DECRYPT);

    jbyteArray rv = ToByteArray(env, out, 8);
    return rv;
}