#include <jni.h>
/* Header for class org_apache_ignite_ml_math_NativeBlasOffHeap */

#ifndef _Included_org_apache_ignite_ml_math_NativeBlasOffHeap
#define _Included_org_apache_ignite_ml_math_NativeBlasOffHeap
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    dasum
 * Signature: (I[DI)D
 */
JNIEXPORT jdouble JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dasum
  (JNIEnv *, jobject, jint, jlong, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    dasum_offsets
 * Signature: (I[DII)D
 */
JNIEXPORT jdouble JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dasum_1offsets
  (JNIEnv *, jobject, jint, jlong, jint, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    daxpy
 * Signature: (ID[DI[DI)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_daxpy
  (JNIEnv *, jobject, jint, jdouble, jlong, jint, jlong, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    daxpy_offsets
 * Signature: (ID[DII[DII)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_daxpy_1offsets
  (JNIEnv *, jobject, jint, jdouble, jlong, jint, jint, jlong, jint, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    dcopy
 * Signature: (I[DI[DI)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dcopy
  (JNIEnv *, jobject, jint, jlong, jint, jlong, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    dcopy_offsets
 * Signature: (I[DII[DII)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dcopy_1offsets
  (JNIEnv *, jobject, jint, jlong, jint, jint, jlong, jint, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    ddot
 * Signature: (I[DI[DI)D
 */
JNIEXPORT jdouble JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_ddot
  (JNIEnv *, jobject, jint, jlong, jint, jlong, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    ddot_offsets
 * Signature: (I[DII[DII)D
 */
JNIEXPORT jdouble JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_ddot_1offsets
  (JNIEnv *, jobject, jint, jlong, jint, jint, jlong, jint, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    dgbmv
 * Signature: (Ljava/lang/String;IIIID[DI[DID[DI)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dgbmv
  (JNIEnv *, jobject, jstring, jint, jint, jint, jint, jdouble, jlong, jint, jlong, jint, jdouble, jlong, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    dgbmv_offsets
 * Signature: (Ljava/lang/String;IIIID[DII[DIID[DII)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dgbmv_1offsets
  (JNIEnv *, jobject, jstring, jint, jint, jint, jint, jdouble, jlong, jint, jint, jlong, jint, jint, jdouble, jlong, jint, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    dgemm
 * Signature: (Ljava/lang/String;Ljava/lang/String;IIID[DI[DID[DI)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dgemm
  (JNIEnv *, jobject, jstring, jstring, jint, jint, jint, jdouble, jlong, jint, jlong, jint, jdouble, jlong, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    dgemm_offsets
 * Signature: (Ljava/lang/String;Ljava/lang/String;IIID[DII[DIID[DII)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dgemm_1offsets
  (JNIEnv *, jobject, jstring, jstring, jint, jint, jint, jdouble, jlong, jint, jint, jlong, jint, jint, jdouble, jlong, jint, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    dgemv
 * Signature: (Ljava/lang/String;IID[DI[DID[DI)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dgemv
  (JNIEnv *, jobject, jstring, jint, jint, jdouble, jlong, jint, jlong, jint, jdouble, jlong, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    dgemv_offsets
 * Signature: (Ljava/lang/String;IID[DII[DIID[DII)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dgemv_1offsets
  (JNIEnv *, jobject, jstring, jint, jint, jdouble, jlong, jint, jint, jlong, jint, jint, jdouble, jlong, jint, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    dger
 * Signature: (IID[DI[DI[DI)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dger
  (JNIEnv *, jobject, jint, jint, jdouble, jlong, jint, jlong, jint, jlong, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    dger_offsets
 * Signature: (IID[DII[DII[DII)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dger_1offsets
  (JNIEnv *, jobject, jint, jint, jdouble, jlong, jint, jint, jlong, jint, jint, jlong, jint, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    dnrm2
 * Signature: (I[DI)D
 */
JNIEXPORT jdouble JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dnrm2
  (JNIEnv *, jobject, jint, jlong, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    dnrm2_offsets
 * Signature: (I[DII)D
 */
JNIEXPORT jdouble JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dnrm2_1offsets
  (JNIEnv *, jobject, jint, jlong, jint, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    drot
 * Signature: (I[DI[DIDD)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_drot
  (JNIEnv *, jobject, jint, jlong, jint, jlong, jint, jdouble, jdouble);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    drot_offsets
 * Signature: (I[DII[DIIDD)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_drot_1offsets
  (JNIEnv *, jobject, jint, jlong, jint, jint, jlong, jint, jint, jdouble, jdouble);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    drotg
 * Signature: (Lorg/netlib/util/doubleW;Lorg/netlib/util/doubleW;Lorg/netlib/util/doubleW;Lorg/netlib/util/doubleW;)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_drotg
  (JNIEnv *, jobject, jobject, jobject, jobject, jobject);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    drotm
 * Signature: (I[DI[DI[D)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_drotm
  (JNIEnv *, jobject, jint, jlong, jint, jlong, jint, jlong);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    drotm_offsets
 * Signature: (I[DII[DII[DI)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_drotm_1offsets
  (JNIEnv *, jobject, jint, jlong, jint, jint, jlong, jint, jint, jlong, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    drotmg
 * Signature: (Lorg/netlib/util/doubleW;Lorg/netlib/util/doubleW;Lorg/netlib/util/doubleW;D[D)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_drotmg
  (JNIEnv *, jobject, jobject, jobject, jobject, jdouble, jlong);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    drotmg_offsets
 * Signature: (Lorg/netlib/util/doubleW;Lorg/netlib/util/doubleW;Lorg/netlib/util/doubleW;D[DI)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_drotmg_1offsets
  (JNIEnv *, jobject, jobject, jobject, jobject, jdouble, jlong, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    dsbmv
 * Signature: (Ljava/lang/String;IID[DI[DID[DI)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dsbmv
  (JNIEnv *, jobject, jstring, jint, jint, jdouble, jlong, jint, jlong, jint, jdouble, jlong, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    dsbmv_offsets
 * Signature: (Ljava/lang/String;IID[DII[DIID[DII)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dsbmv_1offsets
  (JNIEnv *, jobject, jstring, jint, jint, jdouble, jlong, jint, jint, jlong, jint, jint, jdouble, jlong, jint, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    dscal
 * Signature: (ID[DI)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dscal
  (JNIEnv *, jobject, jint, jdouble, jlong, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    dscal_offsets
 * Signature: (ID[DII)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dscal_1offsets
  (JNIEnv *, jobject, jint, jdouble, jlong, jint, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    dspmv
 * Signature: (Ljava/lang/String;ID[D[DID[DI)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dspmv
  (JNIEnv *, jobject, jstring, jint, jdouble, jlong, jlong, jint, jdouble, jlong, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    dspmv_offsets
 * Signature: (Ljava/lang/String;ID[DI[DIID[DII)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dspmv_1offsets
  (JNIEnv *, jobject, jstring, jint, jdouble, jlong, jint, jlong, jint, jint, jdouble, jlong, jint, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    dspr
 * Signature: (Ljava/lang/String;ID[DI[D)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dspr
  (JNIEnv *, jobject, jstring, jint, jdouble, jlong, jint, jlong);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    dspr_offsets
 * Signature: (Ljava/lang/String;ID[DII[DI)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dspr_1offsets
  (JNIEnv *, jobject, jstring, jint, jdouble, jlong, jint, jint, jlong, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    dspr2
 * Signature: (Ljava/lang/String;ID[DI[DI[D)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dspr2
  (JNIEnv *, jobject, jstring, jint, jdouble, jlong, jint, jlong, jint, jlong);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    dspr2_offsets
 * Signature: (Ljava/lang/String;ID[DII[DII[DI)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dspr2_1offsets
  (JNIEnv *, jobject, jstring, jint, jdouble, jlong, jint, jint, jlong, jint, jint, jlong, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    dswap
 * Signature: (I[DI[DI)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dswap
  (JNIEnv *, jobject, jint, jlong, jint, jlong, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    dswap_offsets
 * Signature: (I[DII[DII)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dswap_1offsets
  (JNIEnv *, jobject, jint, jlong, jint, jint, jlong, jint, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    dsymm
 * Signature: (Ljava/lang/String;Ljava/lang/String;IID[DI[DID[DI)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dsymm
  (JNIEnv *, jobject, jstring, jstring, jint, jint, jdouble, jlong, jint, jlong, jint, jdouble, jlong, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    dsymm_offsets
 * Signature: (Ljava/lang/String;Ljava/lang/String;IID[DII[DIID[DII)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dsymm_1offsets
  (JNIEnv *, jobject, jstring, jstring, jint, jint, jdouble, jlong, jint, jint, jlong, jint, jint, jdouble, jlong, jint, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    dsymv
 * Signature: (Ljava/lang/String;ID[DI[DID[DI)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dsymv
  (JNIEnv *, jobject, jstring, jint, jdouble, jlong, jint, jlong, jint, jdouble, jlong, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    dsymv_offsets
 * Signature: (Ljava/lang/String;ID[DII[DIID[DII)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dsymv_1offsets
  (JNIEnv *, jobject, jstring, jint, jdouble, jlong, jint, jint, jlong, jint, jint, jdouble, jlong, jint, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    dsyr
 * Signature: (Ljava/lang/String;ID[DI[DI)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dsyr
  (JNIEnv *, jobject, jstring, jint, jdouble, jlong, jint, jlong, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    dsyr_offsets
 * Signature: (Ljava/lang/String;ID[DII[DII)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dsyr_1offsets
  (JNIEnv *, jobject, jstring, jint, jdouble, jlong, jint, jint, jlong, jint, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    dsyr2
 * Signature: (Ljava/lang/String;ID[DI[DI[DI)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dsyr2
  (JNIEnv *, jobject, jstring, jint, jdouble, jlong, jint, jlong, jint, jlong, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    dsyr2_offsets
 * Signature: (Ljava/lang/String;ID[DII[DII[DII)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dsyr2_1offsets
  (JNIEnv *, jobject, jstring, jint, jdouble, jlong, jint, jint, jlong, jint, jint, jlong, jint, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    dsyr2k
 * Signature: (Ljava/lang/String;Ljava/lang/String;IID[DI[DID[DI)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dsyr2k
  (JNIEnv *, jobject, jstring, jstring, jint, jint, jdouble, jlong, jint, jlong, jint, jdouble, jlong, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    dsyr2k_offsets
 * Signature: (Ljava/lang/String;Ljava/lang/String;IID[DII[DIID[DII)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dsyr2k_1offsets
  (JNIEnv *, jobject, jstring, jstring, jint, jint, jdouble, jlong, jint, jint, jlong, jint, jint, jdouble, jlong, jint, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    dsyrk
 * Signature: (Ljava/lang/String;Ljava/lang/String;IID[DID[DI)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dsyrk
  (JNIEnv *, jobject, jstring, jstring, jint, jint, jdouble, jlong, jint, jdouble, jlong, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    dsyrk_offsets
 * Signature: (Ljava/lang/String;Ljava/lang/String;IID[DIID[DII)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dsyrk_1offsets
  (JNIEnv *, jobject, jstring, jstring, jint, jint, jdouble, jlong, jint, jint, jdouble, jlong, jint, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    dtbmv
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;II[DI[DI)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dtbmv
  (JNIEnv *, jobject, jstring, jstring, jstring, jint, jint, jlong, jint, jlong, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    dtbmv_offsets
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;II[DII[DII)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dtbmv_1offsets
  (JNIEnv *, jobject, jstring, jstring, jstring, jint, jint, jlong, jint, jint, jlong, jint, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    dtbsv
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;II[DI[DI)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dtbsv
  (JNIEnv *, jobject, jstring, jstring, jstring, jint, jint, jlong, jint, jlong, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    dtbsv_offsets
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;II[DII[DII)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dtbsv_1offsets
  (JNIEnv *, jobject, jstring, jstring, jstring, jint, jint, jlong, jint, jint, jlong, jint, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    dtpmv
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;I[D[DI)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dtpmv
  (JNIEnv *, jobject, jstring, jstring, jstring, jint, jlong, jlong, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    dtpmv_offsets
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;I[DI[DII)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dtpmv_1offsets
  (JNIEnv *, jobject, jstring, jstring, jstring, jint, jlong, jint, jlong, jint, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    dtpsv
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;I[D[DI)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dtpsv
  (JNIEnv *, jobject, jstring, jstring, jstring, jint, jlong, jlong, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    dtpsv_offsets
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;I[DI[DII)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dtpsv_1offsets
  (JNIEnv *, jobject, jstring, jstring, jstring, jint, jlong, jint, jlong, jint, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    dtrmm
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;IID[DI[DI)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dtrmm
  (JNIEnv *, jobject, jstring, jstring, jstring, jstring, jint, jint, jdouble, jlong, jint, jlong, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    dtrmm_offsets
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;IID[DII[DII)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dtrmm_1offsets
  (JNIEnv *, jobject, jstring, jstring, jstring, jstring, jint, jint, jdouble, jlong, jint, jint, jlong, jint, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    dtrmv
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;I[DI[DI)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dtrmv
  (JNIEnv *, jobject, jstring, jstring, jstring, jint, jlong, jint, jlong, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    dtrmv_offsets
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;I[DII[DII)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dtrmv_1offsets
  (JNIEnv *, jobject, jstring, jstring, jstring, jint, jlong, jint, jint, jlong, jint, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    dtrsm
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;IID[DI[DI)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dtrsm
  (JNIEnv *, jobject, jstring, jstring, jstring, jstring, jint, jint, jdouble, jlong, jint, jlong, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    dtrsm_offsets
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;IID[DII[DII)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dtrsm_1offsets
  (JNIEnv *, jobject, jstring, jstring, jstring, jstring, jint, jint, jdouble, jlong, jint, jint, jlong, jint, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    dtrsv
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;I[DI[DI)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dtrsv
  (JNIEnv *, jobject, jstring, jstring, jstring, jint, jlong, jint, jlong, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    dtrsv_offsets
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;I[DII[DII)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dtrsv_1offsets
  (JNIEnv *, jobject, jstring, jstring, jstring, jint, jlong, jint, jint, jlong, jint, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    idamax
 * Signature: (I[DI)I
 */
JNIEXPORT jint JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_idamax
  (JNIEnv *, jobject, jint, jlong, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    idamax_offsets
 * Signature: (I[DII)I
 */
JNIEXPORT jint JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_idamax_1offsets
  (JNIEnv *, jobject, jint, jlong, jint, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    isamax
 * Signature: (I[FI)I
 */
JNIEXPORT jint JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_isamax
  (JNIEnv *, jobject, jint, jfloatArray, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    isamax_offsets
 * Signature: (I[FII)I
 */
JNIEXPORT jint JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_isamax_1offsets
  (JNIEnv *, jobject, jint, jfloatArray, jint, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    sasum
 * Signature: (I[FI)F
 */
JNIEXPORT jfloat JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_sasum
  (JNIEnv *, jobject, jint, jfloatArray, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    sasum_offsets
 * Signature: (I[FII)F
 */
JNIEXPORT jfloat JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_sasum_1offsets
  (JNIEnv *, jobject, jint, jfloatArray, jint, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    saxpy
 * Signature: (IF[FI[FI)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_saxpy
  (JNIEnv *, jobject, jint, jfloat, jfloatArray, jint, jfloatArray, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    saxpy_offsets
 * Signature: (IF[FII[FII)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_saxpy_1offsets
  (JNIEnv *, jobject, jint, jfloat, jfloatArray, jint, jint, jfloatArray, jint, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    scopy
 * Signature: (I[FI[FI)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_scopy
  (JNIEnv *, jobject, jint, jfloatArray, jint, jfloatArray, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    scopy_offsets
 * Signature: (I[FII[FII)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_scopy_1offsets
  (JNIEnv *, jobject, jint, jfloatArray, jint, jint, jfloatArray, jint, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    sdot
 * Signature: (I[FI[FI)F
 */
JNIEXPORT jfloat JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_sdot
  (JNIEnv *, jobject, jint, jfloatArray, jint, jfloatArray, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    sdot_offsets
 * Signature: (I[FII[FII)F
 */
JNIEXPORT jfloat JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_sdot_1offsets
  (JNIEnv *, jobject, jint, jfloatArray, jint, jint, jfloatArray, jint, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    sdsdot
 * Signature: (IF[FI[FI)F
 */
JNIEXPORT jfloat JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_sdsdot
  (JNIEnv *, jobject, jint, jfloat, jfloatArray, jint, jfloatArray, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    sdsdot_offsets
 * Signature: (IF[FII[FII)F
 */
JNIEXPORT jfloat JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_sdsdot_1offsets
  (JNIEnv *, jobject, jint, jfloat, jfloatArray, jint, jint, jfloatArray, jint, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    sgbmv
 * Signature: (Ljava/lang/String;IIIIF[FI[FIF[FI)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_sgbmv
  (JNIEnv *, jobject, jstring, jint, jint, jint, jint, jfloat, jfloatArray, jint, jfloatArray, jint, jfloat, jfloatArray, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    sgbmv_offsets
 * Signature: (Ljava/lang/String;IIIIF[FII[FIIF[FII)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_sgbmv_1offsets
  (JNIEnv *, jobject, jstring, jint, jint, jint, jint, jfloat, jfloatArray, jint, jint, jfloatArray, jint, jint, jfloat, jfloatArray, jint, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    sgemm
 * Signature: (Ljava/lang/String;Ljava/lang/String;IIIF[FI[FIF[FI)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_sgemm
  (JNIEnv *, jobject, jstring, jstring, jint, jint, jint, jfloat, jfloatArray, jint, jfloatArray, jint, jfloat, jfloatArray, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    sgemm_offsets
 * Signature: (Ljava/lang/String;Ljava/lang/String;IIIF[FII[FIIF[FII)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_sgemm_1offsets
  (JNIEnv *, jobject, jstring, jstring, jint, jint, jint, jfloat, jfloatArray, jint, jint, jfloatArray, jint, jint, jfloat, jfloatArray, jint, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    sgemv
 * Signature: (Ljava/lang/String;IIF[FI[FIF[FI)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_sgemv
  (JNIEnv *, jobject, jstring, jint, jint, jfloat, jfloatArray, jint, jfloatArray, jint, jfloat, jfloatArray, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    sgemv_offsets
 * Signature: (Ljava/lang/String;IIF[FII[FIIF[FII)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_sgemv_1offsets
  (JNIEnv *, jobject, jstring, jint, jint, jfloat, jfloatArray, jint, jint, jfloatArray, jint, jint, jfloat, jfloatArray, jint, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    sger
 * Signature: (IIF[FI[FI[FI)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_sger
  (JNIEnv *, jobject, jint, jint, jfloat, jfloatArray, jint, jfloatArray, jint, jfloatArray, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    sger_offsets
 * Signature: (IIF[FII[FII[FII)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_sger_1offsets
  (JNIEnv *, jobject, jint, jint, jfloat, jfloatArray, jint, jint, jfloatArray, jint, jint, jfloatArray, jint, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    snrm2
 * Signature: (I[FI)F
 */
JNIEXPORT jfloat JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_snrm2
  (JNIEnv *, jobject, jint, jfloatArray, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    snrm2_offsets
 * Signature: (I[FII)F
 */
JNIEXPORT jfloat JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_snrm2_1offsets
  (JNIEnv *, jobject, jint, jfloatArray, jint, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    srot
 * Signature: (I[FI[FIFF)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_srot
  (JNIEnv *, jobject, jint, jfloatArray, jint, jfloatArray, jint, jfloat, jfloat);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    srot_offsets
 * Signature: (I[FII[FIIFF)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_srot_1offsets
  (JNIEnv *, jobject, jint, jfloatArray, jint, jint, jfloatArray, jint, jint, jfloat, jfloat);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    srotg
 * Signature: (Lorg/netlib/util/floatW;Lorg/netlib/util/floatW;Lorg/netlib/util/floatW;Lorg/netlib/util/floatW;)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_srotg
  (JNIEnv *, jobject, jobject, jobject, jobject, jobject);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    srotm
 * Signature: (I[FI[FI[F)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_srotm
  (JNIEnv *, jobject, jint, jfloatArray, jint, jfloatArray, jint, jfloatArray);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    srotm_offsets
 * Signature: (I[FII[FII[FI)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_srotm_1offsets
  (JNIEnv *, jobject, jint, jfloatArray, jint, jint, jfloatArray, jint, jint, jfloatArray, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    srotmg
 * Signature: (Lorg/netlib/util/floatW;Lorg/netlib/util/floatW;Lorg/netlib/util/floatW;F[F)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_srotmg
  (JNIEnv *, jobject, jobject, jobject, jobject, jfloat, jfloatArray);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    srotmg_offsets
 * Signature: (Lorg/netlib/util/floatW;Lorg/netlib/util/floatW;Lorg/netlib/util/floatW;F[FI)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_srotmg_1offsets
  (JNIEnv *, jobject, jobject, jobject, jobject, jfloat, jfloatArray, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    ssbmv
 * Signature: (Ljava/lang/String;IIF[FI[FIF[FI)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_ssbmv
  (JNIEnv *, jobject, jstring, jint, jint, jfloat, jfloatArray, jint, jfloatArray, jint, jfloat, jfloatArray, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    ssbmv_offsets
 * Signature: (Ljava/lang/String;IIF[FII[FIIF[FII)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_ssbmv_1offsets
  (JNIEnv *, jobject, jstring, jint, jint, jfloat, jfloatArray, jint, jint, jfloatArray, jint, jint, jfloat, jfloatArray, jint, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    sscal
 * Signature: (IF[FI)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_sscal
  (JNIEnv *, jobject, jint, jfloat, jfloatArray, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    sscal_offsets
 * Signature: (IF[FII)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_sscal_1offsets
  (JNIEnv *, jobject, jint, jfloat, jfloatArray, jint, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    sspmv
 * Signature: (Ljava/lang/String;IF[F[FIF[FI)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_sspmv
  (JNIEnv *, jobject, jstring, jint, jfloat, jfloatArray, jfloatArray, jint, jfloat, jfloatArray, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    sspmv_offsets
 * Signature: (Ljava/lang/String;IF[FI[FIIF[FII)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_sspmv_1offsets
  (JNIEnv *, jobject, jstring, jint, jfloat, jfloatArray, jint, jfloatArray, jint, jint, jfloat, jfloatArray, jint, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    sspr
 * Signature: (Ljava/lang/String;IF[FI[F)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_sspr
  (JNIEnv *, jobject, jstring, jint, jfloat, jfloatArray, jint, jfloatArray);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    sspr_offsets
 * Signature: (Ljava/lang/String;IF[FII[FI)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_sspr_1offsets
  (JNIEnv *, jobject, jstring, jint, jfloat, jfloatArray, jint, jint, jfloatArray, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    sspr2
 * Signature: (Ljava/lang/String;IF[FI[FI[F)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_sspr2
  (JNIEnv *, jobject, jstring, jint, jfloat, jfloatArray, jint, jfloatArray, jint, jfloatArray);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    sspr2_offsets
 * Signature: (Ljava/lang/String;IF[FII[FII[FI)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_sspr2_1offsets
  (JNIEnv *, jobject, jstring, jint, jfloat, jfloatArray, jint, jint, jfloatArray, jint, jint, jfloatArray, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    sswap
 * Signature: (I[FI[FI)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_sswap
  (JNIEnv *, jobject, jint, jfloatArray, jint, jfloatArray, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    sswap_offsets
 * Signature: (I[FII[FII)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_sswap_1offsets
  (JNIEnv *, jobject, jint, jfloatArray, jint, jint, jfloatArray, jint, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    ssymm
 * Signature: (Ljava/lang/String;Ljava/lang/String;IIF[FI[FIF[FI)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_ssymm
  (JNIEnv *, jobject, jstring, jstring, jint, jint, jfloat, jfloatArray, jint, jfloatArray, jint, jfloat, jfloatArray, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    ssymm_offsets
 * Signature: (Ljava/lang/String;Ljava/lang/String;IIF[FII[FIIF[FII)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_ssymm_1offsets
  (JNIEnv *, jobject, jstring, jstring, jint, jint, jfloat, jfloatArray, jint, jint, jfloatArray, jint, jint, jfloat, jfloatArray, jint, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    ssymv
 * Signature: (Ljava/lang/String;IF[FI[FIF[FI)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_ssymv
  (JNIEnv *, jobject, jstring, jint, jfloat, jfloatArray, jint, jfloatArray, jint, jfloat, jfloatArray, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    ssymv_offsets
 * Signature: (Ljava/lang/String;IF[FII[FIIF[FII)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_ssymv_1offsets
  (JNIEnv *, jobject, jstring, jint, jfloat, jfloatArray, jint, jint, jfloatArray, jint, jint, jfloat, jfloatArray, jint, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    ssyr
 * Signature: (Ljava/lang/String;IF[FI[FI)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_ssyr
  (JNIEnv *, jobject, jstring, jint, jfloat, jfloatArray, jint, jfloatArray, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    ssyr_offsets
 * Signature: (Ljava/lang/String;IF[FII[FII)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_ssyr_1offsets
  (JNIEnv *, jobject, jstring, jint, jfloat, jfloatArray, jint, jint, jfloatArray, jint, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    ssyr2
 * Signature: (Ljava/lang/String;IF[FI[FI[FI)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_ssyr2
  (JNIEnv *, jobject, jstring, jint, jfloat, jfloatArray, jint, jfloatArray, jint, jfloatArray, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    ssyr2_offsets
 * Signature: (Ljava/lang/String;IF[FII[FII[FII)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_ssyr2_1offsets
  (JNIEnv *, jobject, jstring, jint, jfloat, jfloatArray, jint, jint, jfloatArray, jint, jint, jfloatArray, jint, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    ssyr2k
 * Signature: (Ljava/lang/String;Ljava/lang/String;IIF[FI[FIF[FI)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_ssyr2k
  (JNIEnv *, jobject, jstring, jstring, jint, jint, jfloat, jfloatArray, jint, jfloatArray, jint, jfloat, jfloatArray, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    ssyr2k_offsets
 * Signature: (Ljava/lang/String;Ljava/lang/String;IIF[FII[FIIF[FII)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_ssyr2k_1offsets
  (JNIEnv *, jobject, jstring, jstring, jint, jint, jfloat, jfloatArray, jint, jint, jfloatArray, jint, jint, jfloat, jfloatArray, jint, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    ssyrk
 * Signature: (Ljava/lang/String;Ljava/lang/String;IIF[FIF[FI)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_ssyrk
  (JNIEnv *, jobject, jstring, jstring, jint, jint, jfloat, jfloatArray, jint, jfloat, jfloatArray, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    ssyrk_offsets
 * Signature: (Ljava/lang/String;Ljava/lang/String;IIF[FIIF[FII)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_ssyrk_1offsets
  (JNIEnv *, jobject, jstring, jstring, jint, jint, jfloat, jfloatArray, jint, jint, jfloat, jfloatArray, jint, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    stbmv
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;II[FI[FI)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_stbmv
  (JNIEnv *, jobject, jstring, jstring, jstring, jint, jint, jfloatArray, jint, jfloatArray, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    stbmv_offsets
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;II[FII[FII)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_stbmv_1offsets
  (JNIEnv *, jobject, jstring, jstring, jstring, jint, jint, jfloatArray, jint, jint, jfloatArray, jint, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    stbsv
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;II[FI[FI)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_stbsv
  (JNIEnv *, jobject, jstring, jstring, jstring, jint, jint, jfloatArray, jint, jfloatArray, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    stbsv_offsets
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;II[FII[FII)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_stbsv_1offsets
  (JNIEnv *, jobject, jstring, jstring, jstring, jint, jint, jfloatArray, jint, jint, jfloatArray, jint, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    stpmv
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;I[F[FI)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_stpmv
  (JNIEnv *, jobject, jstring, jstring, jstring, jint, jfloatArray, jfloatArray, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    stpmv_offsets
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;I[FI[FII)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_stpmv_1offsets
  (JNIEnv *, jobject, jstring, jstring, jstring, jint, jfloatArray, jint, jfloatArray, jint, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    stpsv
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;I[F[FI)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_stpsv
  (JNIEnv *, jobject, jstring, jstring, jstring, jint, jfloatArray, jfloatArray, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    stpsv_offsets
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;I[FI[FII)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_stpsv_1offsets
  (JNIEnv *, jobject, jstring, jstring, jstring, jint, jfloatArray, jint, jfloatArray, jint, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    strmm
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;IIF[FI[FI)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_strmm
  (JNIEnv *, jobject, jstring, jstring, jstring, jstring, jint, jint, jfloat, jfloatArray, jint, jfloatArray, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    strmm_offsets
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;IIF[FII[FII)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_strmm_1offsets
  (JNIEnv *, jobject, jstring, jstring, jstring, jstring, jint, jint, jfloat, jfloatArray, jint, jint, jfloatArray, jint, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    strmv
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;I[FI[FI)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_strmv
  (JNIEnv *, jobject, jstring, jstring, jstring, jint, jfloatArray, jint, jfloatArray, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    strmv_offsets
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;I[FII[FII)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_strmv_1offsets
  (JNIEnv *, jobject, jstring, jstring, jstring, jint, jfloatArray, jint, jint, jfloatArray, jint, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    strsm
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;IIF[FI[FI)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_strsm
  (JNIEnv *, jobject, jstring, jstring, jstring, jstring, jint, jint, jfloat, jfloatArray, jint, jfloatArray, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    strsm_offsets
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;IIF[FII[FII)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_strsm_1offsets
  (JNIEnv *, jobject, jstring, jstring, jstring, jstring, jint, jint, jfloat, jfloatArray, jint, jint, jfloatArray, jint, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    strsv
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;I[FI[FI)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_strsv
  (JNIEnv *, jobject, jstring, jstring, jstring, jint, jfloatArray, jint, jfloatArray, jint);

/*
 * Class:     org_apache_ignite_ml_math_NativeBlasOffHeap
 * Method:    strsv_offsets
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;I[FII[FII)V
 */
JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_strsv_1offsets
  (JNIEnv *, jobject, jstring, jstring, jstring, jint, jfloatArray, jint, jint, jfloatArray, jint, jint);

#ifdef __cplusplus
}
#endif
#endif
