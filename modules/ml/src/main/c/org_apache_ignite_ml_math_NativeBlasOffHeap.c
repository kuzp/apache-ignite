/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <jni.h>
#include "netlib-jni.h"
#include <cblas.h>
#include "org_apache_ignite_ml_math_NativeBlasOffHeap.h"

JNIEXPORT jdouble JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dasum (JNIEnv * env, jobject calling_obj, jint n, jlong dx, jint incx) {
  jdouble * jni_dx = (jdouble *)dx;
  jdouble returnValue = cblas_dasum(n, jni_dx, incx);
  return returnValue;
}

JNIEXPORT jdouble JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dasum_1offsets (JNIEnv * env, jobject calling_obj, jint n, jlong dx, jint _dx_offset, jint incx) {
  jdouble * jni_dx = (jdouble *)dx;
  jdouble returnValue = cblas_dasum(n, jni_dx + _dx_offset, incx);
  return returnValue;
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_daxpy (JNIEnv * env, jobject calling_obj, jint n, jdouble da, jlong dx, jint incx, jlong dy, jint incy) {
  jdouble * jni_dx = (jdouble *)dx;
  jdouble * jni_dy = (jdouble *)dy;
  cblas_daxpy(n, da, jni_dx, incx, jni_dy, incy);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_daxpy_1offsets (JNIEnv * env, jobject calling_obj, jint n, jdouble da, jlong dx, jint _dx_offset, jint incx, jlong dy, jint _dy_offset, jint incy) {
  jdouble * jni_dx = (jdouble *)dx;
  jdouble * jni_dy = (jdouble *)dy;
  cblas_daxpy(n, da, jni_dx + _dx_offset, incx, jni_dy + _dy_offset, incy);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dcopy (JNIEnv * env, jobject calling_obj, jint n, jlong dx, jint incx, jlong dy, jint incy) {
  jdouble * jni_dx = (jdouble *)dx;
  jdouble * jni_dy = (jdouble *)dy;
cblas_dcopy(n, jni_dx, incx, jni_dy, incy);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dcopy_1offsets (JNIEnv * env, jobject calling_obj, jint n, jlong dx, jint _dx_offset, jint incx, jlong dy, jint _dy_offset, jint incy) {
  jdouble * jni_dx = (jdouble *)dx;
  jdouble * jni_dy = (jdouble *)dy;
cblas_dcopy(n, jni_dx + _dx_offset, incx, jni_dy + _dy_offset, incy);
}

JNIEXPORT jdouble JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_ddot (JNIEnv * env, jobject calling_obj, jint n, jlong dx, jint incx, jlong dy, jint incy) {
  jdouble * jni_dx = (jdouble *)dx;
  jdouble * jni_dy = (jdouble *)dy;
  jdouble returnValue = cblas_ddot(n, jni_dx, incx, jni_dy, incy);
  return returnValue;
}

JNIEXPORT jdouble JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_ddot_1offsets (JNIEnv * env, jobject calling_obj, jint n, jlong dx, jint _dx_offset, jint incx, jlong dy, jint _dy_offset, jint incy) {
  jdouble * jni_dx = (jdouble *)dx;
  jdouble * jni_dy = (jdouble *)dy;
  jdouble returnValue = cblas_ddot(n, jni_dx + _dx_offset, incx, jni_dy + _dy_offset, incy);
  return returnValue;
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dgbmv (JNIEnv * env, jobject calling_obj, jstring trans, jint m, jint n, jint kl, jint ku, jdouble alpha, jlong a, jint lda, jlong x, jint incx, jdouble beta, jlong y, jint incy) {
  char * jni_trans = (char *)(*env)->GetStringUTFChars(env, trans, JNI_FALSE);
  jdouble * jni_a = (jdouble *)a;
  jdouble * jni_x = (jdouble *)x;
  jdouble * jni_y = (jdouble *)x;
cblas_dgbmv(CblasColMajor, getCblasTrans(jni_trans), m, n, kl, ku, alpha, jni_a, lda, jni_x, incx, beta, jni_y, incy);
  (*env)->ReleaseStringUTFChars(env, trans, jni_trans);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dgbmv_1offsets (JNIEnv * env, jobject calling_obj, jstring trans, jint m, jint n, jint kl, jint ku, jdouble alpha, jlong a, jint _a_offset, jint lda, jlong x, jint _x_offset, jint incx, jdouble beta, jlong y, jint _y_offset, jint incy) {
  char * jni_trans = (char *)(*env)->GetStringUTFChars(env, trans, JNI_FALSE);
  jdouble * jni_a = (jdouble *)a;
  jdouble * jni_x = (jdouble *)x;
  jdouble * jni_y = (jdouble *)x;
cblas_dgbmv(CblasColMajor, getCblasTrans(jni_trans), m, n, kl, ku, alpha, jni_a + _a_offset, lda, jni_x + _x_offset, incx, beta, jni_y + _y_offset, incy);
  (*env)->ReleaseStringUTFChars(env, trans, jni_trans);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dgemm (JNIEnv * env, jobject calling_obj, jstring transa, jstring transb, jint m, jint n, jint k, jdouble alpha, jlong a, jint lda, jlong b, jint ldb, jdouble beta, jlong c, jint Ldc) {
  char * jni_transa = (char *)(*env)->GetStringUTFChars(env, transa, JNI_FALSE);
  char * jni_transb = (char *)(*env)->GetStringUTFChars(env, transb, JNI_FALSE);
  jdouble * jni_a = (jdouble *)a;
  jdouble * jni_b = (jdouble *)b;
  jdouble * jni_c = (jdouble *)c;
cblas_dgemm(CblasColMajor, getCblasTrans(jni_transa), getCblasTrans(jni_transb), m, n, k, alpha, jni_a, lda, jni_b, ldb, beta, jni_c, Ldc);
  (*env)->ReleaseStringUTFChars(env, transb, jni_transb);
  (*env)->ReleaseStringUTFChars(env, transa, jni_transa);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dgemm_1offsets (JNIEnv * env, jobject calling_obj, jstring transa, jstring transb, jint m, jint n, jint k, jdouble alpha, jlong a, jint _a_offset, jint lda, jlong b, jint _b_offset, jint ldb, jdouble beta, jlong c, jint _c_offset, jint Ldc) {
  char * jni_transa = (char *)(*env)->GetStringUTFChars(env, transa, JNI_FALSE);
  char * jni_transb = (char *)(*env)->GetStringUTFChars(env, transb, JNI_FALSE);
  jdouble * jni_a = (jdouble *)a;
  jdouble * jni_b = (jdouble *)b;
  jdouble * jni_c = (jdouble *)c;
cblas_dgemm(CblasColMajor, getCblasTrans(jni_transa), getCblasTrans(jni_transb), m, n, k, alpha, jni_a + _a_offset, lda, jni_b + _b_offset, ldb, beta, jni_c + _c_offset, Ldc);
  (*env)->ReleaseStringUTFChars(env, transb, jni_transb);
  (*env)->ReleaseStringUTFChars(env, transa, jni_transa);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dgemv (JNIEnv * env, jobject calling_obj, jstring trans, jint m, jint n, jdouble alpha, jlong a, jint lda, jlong x, jint incx, jdouble beta, jlong y, jint incy) {
  char * jni_trans = (char *)(*env)->GetStringUTFChars(env, trans, JNI_FALSE);
  jdouble * jni_a = (jdouble *)a;
  jdouble * jni_x = (jdouble *)x;
  jdouble * jni_y = (jdouble *)x;
cblas_dgemv(CblasColMajor, getCblasTrans(jni_trans), m, n, alpha, jni_a, lda, jni_x, incx, beta, jni_y, incy);
  (*env)->ReleaseStringUTFChars(env, trans, jni_trans);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dgemv_1offsets (JNIEnv * env, jobject calling_obj, jstring trans, jint m, jint n, jdouble alpha, jlong a, jint _a_offset, jint lda, jlong x, jint _x_offset, jint incx, jdouble beta, jlong y, jint _y_offset, jint incy) {
  char * jni_trans = (char *)(*env)->GetStringUTFChars(env, trans, JNI_FALSE);
  jdouble * jni_a = (jdouble *)a;
  jdouble * jni_x = (jdouble *)x;
  jdouble * jni_y = (jdouble *)x;
cblas_dgemv(CblasColMajor, getCblasTrans(jni_trans), m, n, alpha, jni_a + _a_offset, lda, jni_x + _x_offset, incx, beta, jni_y + _y_offset, incy);
  (*env)->ReleaseStringUTFChars(env, trans, jni_trans);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dger (JNIEnv * env, jobject calling_obj, jint m, jint n, jdouble alpha, jlong x, jint incx, jlong y, jint incy, jlong a, jint lda) {
  jdouble * jni_x = (jdouble *)x;
  jdouble * jni_y = (jdouble *)x;
  jdouble * jni_a = (jdouble *)a;
cblas_dger(CblasColMajor, m, n, alpha, jni_x, incx, jni_y, incy, jni_a, lda);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dger_1offsets (JNIEnv * env, jobject calling_obj, jint m, jint n, jdouble alpha, jlong x, jint _x_offset, jint incx, jlong y, jint _y_offset, jint incy, jlong a, jint _a_offset, jint lda) {
  jdouble * jni_x = (jdouble *)x;
  jdouble * jni_y = (jdouble *)x;
  jdouble * jni_a = (jdouble *)a;
cblas_dger(CblasColMajor, m, n, alpha, jni_x + _x_offset, incx, jni_y + _y_offset, incy, jni_a + _a_offset, lda);
}

JNIEXPORT jdouble JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dnrm2 (JNIEnv * env, jobject calling_obj, jint n, jlong x, jint incx) {
  jdouble * jni_x = (jdouble *)x;
  jdouble returnValue = cblas_dnrm2(n, jni_x, incx);
  return returnValue;
}

JNIEXPORT jdouble JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dnrm2_1offsets (JNIEnv * env, jobject calling_obj, jint n, jlong x, jint _x_offset, jint incx) {
  jdouble * jni_x = (jdouble *)x;
  jdouble returnValue = cblas_dnrm2(n, jni_x + _x_offset, incx);
  return returnValue;
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_drot (JNIEnv * env, jobject calling_obj, jint n, jlong dx, jint incx, jlong dy, jint incy, jdouble c, jdouble s) {
  jdouble * jni_dx = (jdouble *)dx;
  jdouble * jni_dy = (jdouble *)dy;
cblas_drot(n, jni_dx, incx, jni_dy, incy, c, s);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_drot_1offsets (JNIEnv * env, jobject calling_obj, jint n, jlong dx, jint _dx_offset, jint incx, jlong dy, jint _dy_offset, jint incy, jdouble c, jdouble s) {
  jdouble * jni_dx = (jdouble *)dx;
  jdouble * jni_dy = (jdouble *)dy;
cblas_drot(n, jni_dx + _dx_offset, incx, jni_dy + _dy_offset, incy, c, s);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_drotg (JNIEnv * env, jobject calling_obj, jdoublew da, jdoublew db, jdoublew c, jdoublew s) {
  jclass jni_daClass = (*env)->GetObjectClass(env, da);
  jfieldID jni_daId = (*env)->GetFieldID(env, jni_daClass, "val", "D");
  jdouble jni_da = (*env)->GetDoubleField(env, da, jni_daId);
  jclass jni_dbClass = (*env)->GetObjectClass(env, db);
  jfieldID jni_dbId = (*env)->GetFieldID(env, jni_dbClass, "val", "D");
  jdouble jni_db = (*env)->GetDoubleField(env, db, jni_dbId);
  jclass jni_cClass = (*env)->GetObjectClass(env, c);
  jfieldID jni_cId = (*env)->GetFieldID(env, jni_cClass, "val", "D");
  jdouble jni_c = (*env)->GetDoubleField(env, c, jni_cId);
  jclass jni_sClass = (*env)->GetObjectClass(env, s);
  jfieldID jni_sId = (*env)->GetFieldID(env, jni_sClass, "val", "D");
  jdouble jni_s = (*env)->GetDoubleField(env, s, jni_sId);
cblas_drotg(&jni_da, &jni_db, &jni_c, &jni_s);
  (*env)->SetDoubleField(env, s, jni_sId, jni_s);
  (*env)->SetDoubleField(env, c, jni_cId, jni_c);
  (*env)->SetDoubleField(env, db, jni_dbId, jni_db);
  (*env)->SetDoubleField(env, da, jni_daId, jni_da);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_drotm (JNIEnv * env, jobject calling_obj, jint n, jlong dx, jint incx, jlong dy, jint incy, jlong dparam) {
  jdouble * jni_dx = (jdouble *)dx;
  jdouble * jni_dy = (jdouble *)dy;
  jdouble * jni_dparam = (jdouble *)dparam;
cblas_drotm(n, jni_dx, incx, jni_dy, incy, jni_dparam);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_drotm_1offsets (JNIEnv * env, jobject calling_obj, jint n, jlong dx, jint _dx_offset, jint incx, jlong dy, jint _dy_offset, jint incy, jlong dparam, jint _dparam_offset) {
  jdouble * jni_dx = (jdouble *)dx;
  jdouble * jni_dy = (jdouble *)dy;
  jdouble * jni_dparam = (jdouble *)dparam;
cblas_drotm(n, jni_dx + _dx_offset, incx, jni_dy + _dy_offset, incy, jni_dparam + _dparam_offset);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_drotmg (JNIEnv * env, jobject calling_obj, jdoublew dd1, jdoublew dd2, jdoublew dx1, jdouble dy1, jlong dparam) {
  jclass jni_dd1Class = (*env)->GetObjectClass(env, dd1);
  jfieldID jni_dd1Id = (*env)->GetFieldID(env, jni_dd1Class, "val", "D");
  jdouble jni_dd1 = (*env)->GetDoubleField(env, dd1, jni_dd1Id);
  jclass jni_dd2Class = (*env)->GetObjectClass(env, dd2);
  jfieldID jni_dd2Id = (*env)->GetFieldID(env, jni_dd2Class, "val", "D");
  jdouble jni_dd2 = (*env)->GetDoubleField(env, dd2, jni_dd2Id);
  jclass jni_dx1Class = (*env)->GetObjectClass(env, dx1);
  jfieldID jni_dx1Id = (*env)->GetFieldID(env, jni_dx1Class, "val", "D");
  jdouble jni_dx1 = (*env)->GetDoubleField(env, dx1, jni_dx1Id);
  jdouble * jni_dparam = (jdouble *)dparam;
cblas_drotmg(&jni_dd1, &jni_dd2, &jni_dx1, dy1, jni_dparam);
  (*env)->SetDoubleField(env, dx1, jni_dx1Id, jni_dx1);
  (*env)->SetDoubleField(env, dd2, jni_dd2Id, jni_dd2);
  (*env)->SetDoubleField(env, dd1, jni_dd1Id, jni_dd1);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_drotmg_1offsets (JNIEnv * env, jobject calling_obj, jdoublew dd1, jdoublew dd2, jdoublew dx1, jdouble dy1, jlong dparam, jint _dparam_offset) {
  jclass jni_dd1Class = (*env)->GetObjectClass(env, dd1);
  jfieldID jni_dd1Id = (*env)->GetFieldID(env, jni_dd1Class, "val", "D");
  jdouble jni_dd1 = (*env)->GetDoubleField(env, dd1, jni_dd1Id);
  jclass jni_dd2Class = (*env)->GetObjectClass(env, dd2);
  jfieldID jni_dd2Id = (*env)->GetFieldID(env, jni_dd2Class, "val", "D");
  jdouble jni_dd2 = (*env)->GetDoubleField(env, dd2, jni_dd2Id);
  jclass jni_dx1Class = (*env)->GetObjectClass(env, dx1);
  jfieldID jni_dx1Id = (*env)->GetFieldID(env, jni_dx1Class, "val", "D");
  jdouble jni_dx1 = (*env)->GetDoubleField(env, dx1, jni_dx1Id);
  jdouble * jni_dparam = (jdouble *)dparam;
cblas_drotmg(&jni_dd1, &jni_dd2, &jni_dx1, dy1, jni_dparam + _dparam_offset);
  (*env)->SetDoubleField(env, dx1, jni_dx1Id, jni_dx1);
  (*env)->SetDoubleField(env, dd2, jni_dd2Id, jni_dd2);
  (*env)->SetDoubleField(env, dd1, jni_dd1Id, jni_dd1);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dsbmv (JNIEnv * env, jobject calling_obj, jstring uplo, jint n, jint k, jdouble alpha, jlong a, jint lda, jlong x, jint incx, jdouble beta, jlong y, jint incy) {
  char * jni_uplo = (char *)(*env)->GetStringUTFChars(env, uplo, JNI_FALSE);
  jdouble * jni_a = (jdouble *)a;
  jdouble * jni_x = (jdouble *)x;
  jdouble * jni_y = (jdouble *)x;
cblas_dsbmv(CblasColMajor, getCblasUpLo(jni_uplo), n, k, alpha, jni_a, lda, jni_x, incx, beta, jni_y, incy);
  (*env)->ReleaseStringUTFChars(env, uplo, jni_uplo);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dsbmv_1offsets (JNIEnv * env, jobject calling_obj, jstring uplo, jint n, jint k, jdouble alpha, jlong a, jint _a_offset, jint lda, jlong x, jint _x_offset, jint incx, jdouble beta, jlong y, jint _y_offset, jint incy) {
  char * jni_uplo = (char *)(*env)->GetStringUTFChars(env, uplo, JNI_FALSE);
  jdouble * jni_a = (jdouble *)a;
  jdouble * jni_x = (jdouble *)x;
  jdouble * jni_y = (jdouble *)x;
cblas_dsbmv(CblasColMajor, getCblasUpLo(jni_uplo), n, k, alpha, jni_a + _a_offset, lda, jni_x + _x_offset, incx, beta, jni_y + _y_offset, incy);
  (*env)->ReleaseStringUTFChars(env, uplo, jni_uplo);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dscal (JNIEnv * env, jobject calling_obj, jint n, jdouble da, jlong dx, jint incx) {
  jdouble * jni_dx = (jdouble *)dx;
cblas_dscal(n, da, jni_dx, incx);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dscal_1offsets (JNIEnv * env, jobject calling_obj, jint n, jdouble da, jlong dx, jint _dx_offset, jint incx) {
  jdouble * jni_dx = (jdouble *)dx;
cblas_dscal(n, da, jni_dx + _dx_offset, incx);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dspmv (JNIEnv * env, jobject calling_obj, jstring uplo, jint n, jdouble alpha, jlong ap, jlong x, jint incx, jdouble beta, jlong y, jint incy) {
  char * jni_uplo = (char *)(*env)->GetStringUTFChars(env, uplo, JNI_FALSE);
  jdouble * jni_ap = (jdouble *)ap;
  jdouble * jni_x = (jdouble *)x;
  jdouble * jni_y = (jdouble *)x;
cblas_dspmv(CblasColMajor, getCblasUpLo(jni_uplo), n, alpha, jni_ap, jni_x, incx, beta, jni_y, incy);
  (*env)->ReleaseStringUTFChars(env, uplo, jni_uplo);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dspmv_1offsets (JNIEnv * env, jobject calling_obj, jstring uplo, jint n, jdouble alpha, jlong ap, jint _ap_offset, jlong x, jint _x_offset, jint incx, jdouble beta, jlong y, jint _y_offset, jint incy) {
  char * jni_uplo = (char *)(*env)->GetStringUTFChars(env, uplo, JNI_FALSE);
  jdouble * jni_ap = (jdouble *)ap;
  jdouble * jni_x = (jdouble *)x;
  jdouble * jni_y = (jdouble *)x;
cblas_dspmv(CblasColMajor, getCblasUpLo(jni_uplo), n, alpha, jni_ap + _ap_offset, jni_x + _x_offset, incx, beta, jni_y + _y_offset, incy);
  (*env)->ReleaseStringUTFChars(env, uplo, jni_uplo);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dspr (JNIEnv * env, jobject calling_obj, jstring uplo, jint n, jdouble alpha, jlong x, jint incx, jlong ap) {
  char * jni_uplo = (char *)(*env)->GetStringUTFChars(env, uplo, JNI_FALSE);
  jdouble * jni_x = (jdouble *)x;
  jdouble * jni_ap = (jdouble *)ap;
cblas_dspr(CblasColMajor, getCblasUpLo(jni_uplo), n, alpha, jni_x, incx, jni_ap);
  (*env)->ReleaseStringUTFChars(env, uplo, jni_uplo);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dspr_1offsets (JNIEnv * env, jobject calling_obj, jstring uplo, jint n, jdouble alpha, jlong x, jint _x_offset, jint incx, jlong ap, jint _ap_offset) {
  char * jni_uplo = (char *)(*env)->GetStringUTFChars(env, uplo, JNI_FALSE);
  jdouble * jni_x = (jdouble *)x;
  jdouble * jni_ap = (jdouble *)ap;
cblas_dspr(CblasColMajor, getCblasUpLo(jni_uplo), n, alpha, jni_x + _x_offset, incx, jni_ap + _ap_offset);
  (*env)->ReleaseStringUTFChars(env, uplo, jni_uplo);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dspr2 (JNIEnv * env, jobject calling_obj, jstring uplo, jint n, jdouble alpha, jlong x, jint incx, jlong y, jint incy, jlong ap) {
  char * jni_uplo = (char *)(*env)->GetStringUTFChars(env, uplo, JNI_FALSE);
  jdouble * jni_x = (jdouble *)x;
  jdouble * jni_y = (jdouble *)x;
  jdouble * jni_ap = (jdouble *)ap;
cblas_dspr2(CblasColMajor, getCblasUpLo(jni_uplo), n, alpha, jni_x, incx, jni_y, incy, jni_ap);
  (*env)->ReleaseStringUTFChars(env, uplo, jni_uplo);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dspr2_1offsets (JNIEnv * env, jobject calling_obj, jstring uplo, jint n, jdouble alpha, jlong x, jint _x_offset, jint incx, jlong y, jint _y_offset, jint incy, jlong ap, jint _ap_offset) {
  char * jni_uplo = (char *)(*env)->GetStringUTFChars(env, uplo, JNI_FALSE);
  jdouble * jni_x = (jdouble *)x;
  jdouble * jni_y = (jdouble *)x;
  jdouble * jni_ap = (jdouble *)ap;
cblas_dspr2(CblasColMajor, getCblasUpLo(jni_uplo), n, alpha, jni_x + _x_offset, incx, jni_y + _y_offset, incy, jni_ap + _ap_offset);
  (*env)->ReleaseStringUTFChars(env, uplo, jni_uplo);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dswap (JNIEnv * env, jobject calling_obj, jint n, jlong dx, jint incx, jlong dy, jint incy) {
  jdouble * jni_dx = (jdouble *)dx;
  jdouble * jni_dy = (jdouble *)dy;
cblas_dswap(n, jni_dx, incx, jni_dy, incy);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dswap_1offsets (JNIEnv * env, jobject calling_obj, jint n, jlong dx, jint _dx_offset, jint incx, jlong dy, jint _dy_offset, jint incy) {
  jdouble * jni_dx = (jdouble *)dx;
  jdouble * jni_dy = (jdouble *)dy;
cblas_dswap(n, jni_dx + _dx_offset, incx, jni_dy + _dy_offset, incy);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dsymm (JNIEnv * env, jobject calling_obj, jstring side, jstring uplo, jint m, jint n, jdouble alpha, jlong a, jint lda, jlong b, jint ldb, jdouble beta, jlong c, jint Ldc) {
  char * jni_side = (char *)(*env)->GetStringUTFChars(env, side, JNI_FALSE);
  char * jni_uplo = (char *)(*env)->GetStringUTFChars(env, uplo, JNI_FALSE);
  jdouble * jni_a = (jdouble *)a;
  jdouble * jni_b = (jdouble *)b;
  jdouble * jni_c = (jdouble *)c;
cblas_dsymm(CblasColMajor, getCblasSide(jni_side), getCblasUpLo(jni_uplo), m, n, alpha, jni_a, lda, jni_b, ldb, beta, jni_c, Ldc);
  (*env)->ReleaseStringUTFChars(env, uplo, jni_uplo);
  (*env)->ReleaseStringUTFChars(env, side, jni_side);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dsymm_1offsets (JNIEnv * env, jobject calling_obj, jstring side, jstring uplo, jint m, jint n, jdouble alpha, jlong a, jint _a_offset, jint lda, jlong b, jint _b_offset, jint ldb, jdouble beta, jlong c, jint _c_offset, jint Ldc) {
  char * jni_side = (char *)(*env)->GetStringUTFChars(env, side, JNI_FALSE);
  char * jni_uplo = (char *)(*env)->GetStringUTFChars(env, uplo, JNI_FALSE);
  jdouble * jni_a = (jdouble *)a;
  jdouble * jni_b = (jdouble *)b;
  double * jni_c = (jdouble *)c;
cblas_dsymm(CblasColMajor, getCblasSide(jni_side), getCblasUpLo(jni_uplo), m, n, alpha, jni_a + _a_offset, lda, jni_b + _b_offset, ldb, beta, jni_c + _c_offset, Ldc);
  (*env)->ReleaseStringUTFChars(env, uplo, jni_uplo);
  (*env)->ReleaseStringUTFChars(env, side, jni_side);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dsymv (JNIEnv * env, jobject calling_obj, jstring uplo, jint n, jdouble alpha, jlong a, jint lda, jlong x, jint incx, jdouble beta, jlong y, jint incy) {
  char * jni_uplo = (char *)(*env)->GetStringUTFChars(env, uplo, JNI_FALSE);
  jdouble * jni_a = (jdouble *)a;
  jdouble * jni_x = (jdouble *)x;
  jdouble * jni_y = (jdouble *)x;
cblas_dsymv(CblasColMajor, getCblasUpLo(jni_uplo), n, alpha, jni_a, lda, jni_x, incx, beta, jni_y, incy);
  (*env)->ReleaseStringUTFChars(env, uplo, jni_uplo);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dsymv_1offsets (JNIEnv * env, jobject calling_obj, jstring uplo, jint n, jdouble alpha, jlong a, jint _a_offset, jint lda, jlong x, jint _x_offset, jint incx, jdouble beta, jlong y, jint _y_offset, jint incy) {
  char * jni_uplo = (char *)(*env)->GetStringUTFChars(env, uplo, JNI_FALSE);
  jdouble * jni_a = (jdouble *)a;
  jdouble * jni_x = (jdouble *)x;
  jdouble * jni_y = (jdouble *)x;
cblas_dsymv(CblasColMajor, getCblasUpLo(jni_uplo), n, alpha, jni_a + _a_offset, lda, jni_x + _x_offset, incx, beta, jni_y + _y_offset, incy);
  (*env)->ReleaseStringUTFChars(env, uplo, jni_uplo);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dsyr (JNIEnv * env, jobject calling_obj, jstring uplo, jint n, jdouble alpha, jlong x, jint incx, jlong a, jint lda) {
  char * jni_uplo = (char *)(*env)->GetStringUTFChars(env, uplo, JNI_FALSE);
  jdouble * jni_x = (jdouble *)x;
  jdouble * jni_a = (jdouble *)a;
cblas_dsyr(CblasColMajor, getCblasUpLo(jni_uplo), n, alpha, jni_x, incx, jni_a, lda);
  (*env)->ReleaseStringUTFChars(env, uplo, jni_uplo);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dsyr_1offsets (JNIEnv * env, jobject calling_obj, jstring uplo, jint n, jdouble alpha, jlong x, jint _x_offset, jint incx, jlong a, jint _a_offset, jint lda) {
  char * jni_uplo = (char *)(*env)->GetStringUTFChars(env, uplo, JNI_FALSE);
  jdouble * jni_x = (jdouble *)x;
  jdouble * jni_a = (jdouble *)a;
cblas_dsyr(CblasColMajor, getCblasUpLo(jni_uplo), n, alpha, jni_x + _x_offset, incx, jni_a + _a_offset, lda);
  (*env)->ReleaseStringUTFChars(env, uplo, jni_uplo);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dsyr2 (JNIEnv * env, jobject calling_obj, jstring uplo, jint n, jdouble alpha, jlong x, jint incx, jlong y, jint incy, jlong a, jint lda) {
  char * jni_uplo = (char *)(*env)->GetStringUTFChars(env, uplo, JNI_FALSE);
  jdouble * jni_x = (jdouble *)x;
  jdouble * jni_y = (jdouble *)x;
  jdouble * jni_a = (jdouble *)a;
cblas_dsyr2(CblasColMajor, getCblasUpLo(jni_uplo), n, alpha, jni_x, incx, jni_y, incy, jni_a, lda);
  (*env)->ReleaseStringUTFChars(env, uplo, jni_uplo);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dsyr2_1offsets (JNIEnv * env, jobject calling_obj, jstring uplo, jint n, jdouble alpha, jlong x, jint _x_offset, jint incx, jlong y, jint _y_offset, jint incy, jlong a, jint _a_offset, jint lda) {
  char * jni_uplo = (char *)(*env)->GetStringUTFChars(env, uplo, JNI_FALSE);
  jdouble * jni_x = (jdouble *)x;
  jdouble * jni_y = (jdouble *)x;
  jdouble * jni_a = (jdouble *)a;
cblas_dsyr2(CblasColMajor, getCblasUpLo(jni_uplo), n, alpha, jni_x + _x_offset, incx, jni_y + _y_offset, incy, jni_a + _a_offset, lda);
  (*env)->ReleaseStringUTFChars(env, uplo, jni_uplo);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dsyr2k (JNIEnv * env, jobject calling_obj, jstring uplo, jstring trans, jint n, jint k, jdouble alpha, jlong a, jint lda, jlong b, jint ldb, jdouble beta, jlong c, jint Ldc) {
  char * jni_uplo = (char *)(*env)->GetStringUTFChars(env, uplo, JNI_FALSE);
  char * jni_trans = (char *)(*env)->GetStringUTFChars(env, trans, JNI_FALSE);
  jdouble * jni_a = (jdouble *)a;
  jdouble * jni_b = (jdouble *)b;
  double * jni_c = (jdouble *)c;
cblas_dsyr2k(CblasColMajor, getCblasUpLo(jni_uplo), getCblasTrans(jni_trans), n, k, alpha, jni_a, lda, jni_b, ldb, beta, jni_c, Ldc);
  (*env)->ReleaseStringUTFChars(env, trans, jni_trans);
  (*env)->ReleaseStringUTFChars(env, uplo, jni_uplo);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dsyr2k_1offsets (JNIEnv * env, jobject calling_obj, jstring uplo, jstring trans, jint n, jint k, jdouble alpha, jlong a, jint _a_offset, jint lda, jlong b, jint _b_offset, jint ldb, jdouble beta, jlong c, jint _c_offset, jint Ldc) {
  char * jni_uplo = (char *)(*env)->GetStringUTFChars(env, uplo, JNI_FALSE);
  char * jni_trans = (char *)(*env)->GetStringUTFChars(env, trans, JNI_FALSE);
  jdouble * jni_a = (jdouble *)a;
  jdouble * jni_b = (jdouble *)b;
  double * jni_c = (jdouble *)c;
cblas_dsyr2k(CblasColMajor, getCblasUpLo(jni_uplo), getCblasTrans(jni_trans), n, k, alpha, jni_a + _a_offset, lda, jni_b + _b_offset, ldb, beta, jni_c + _c_offset, Ldc);
  (*env)->ReleaseStringUTFChars(env, trans, jni_trans);
  (*env)->ReleaseStringUTFChars(env, uplo, jni_uplo);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dsyrk (JNIEnv * env, jobject calling_obj, jstring uplo, jstring trans, jint n, jint k, jdouble alpha, jlong a, jint lda, jdouble beta, jlong c, jint Ldc) {
  char * jni_uplo = (char *)(*env)->GetStringUTFChars(env, uplo, JNI_FALSE);
  char * jni_trans = (char *)(*env)->GetStringUTFChars(env, trans, JNI_FALSE);
  jdouble * jni_a = (jdouble *)a;
  double * jni_c = (jdouble *)c;
cblas_dsyrk(CblasColMajor, getCblasUpLo(jni_uplo), getCblasTrans(jni_trans), n, k, alpha, jni_a, lda, beta, jni_c, Ldc);
  (*env)->ReleaseStringUTFChars(env, trans, jni_trans);
  (*env)->ReleaseStringUTFChars(env, uplo, jni_uplo);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dsyrk_1offsets (JNIEnv * env, jobject calling_obj, jstring uplo, jstring trans, jint n, jint k, jdouble alpha, jlong a, jint _a_offset, jint lda, jdouble beta, jlong c, jint _c_offset, jint Ldc) {
  char * jni_uplo = (char *)(*env)->GetStringUTFChars(env, uplo, JNI_FALSE);
  char * jni_trans = (char *)(*env)->GetStringUTFChars(env, trans, JNI_FALSE);
  jdouble * jni_a = (jdouble *)a;
  double * jni_c = (jdouble *)c;
cblas_dsyrk(CblasColMajor, getCblasUpLo(jni_uplo), getCblasTrans(jni_trans), n, k, alpha, jni_a + _a_offset, lda, beta, jni_c + _c_offset, Ldc);
  (*env)->ReleaseStringUTFChars(env, trans, jni_trans);
  (*env)->ReleaseStringUTFChars(env, uplo, jni_uplo);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dtbmv (JNIEnv * env, jobject calling_obj, jstring uplo, jstring trans, jstring diag, jint n, jint k, jlong a, jint lda, jlong x, jint incx) {
  char * jni_uplo = (char *)(*env)->GetStringUTFChars(env, uplo, JNI_FALSE);
  char * jni_trans = (char *)(*env)->GetStringUTFChars(env, trans, JNI_FALSE);
  char * jni_diag = (char *)(*env)->GetStringUTFChars(env, diag, JNI_FALSE);
  jdouble * jni_a = (jdouble *)a;
  jdouble * jni_x = (jdouble *)x;
cblas_dtbmv(CblasColMajor, getCblasUpLo(jni_uplo), getCblasTrans(jni_trans), getCblasDiag(jni_diag), n, k, jni_a, lda, jni_x, incx);
  (*env)->ReleaseStringUTFChars(env, diag, jni_diag);
  (*env)->ReleaseStringUTFChars(env, trans, jni_trans);
  (*env)->ReleaseStringUTFChars(env, uplo, jni_uplo);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dtbmv_1offsets (JNIEnv * env, jobject calling_obj, jstring uplo, jstring trans, jstring diag, jint n, jint k, jlong a, jint _a_offset, jint lda, jlong x, jint _x_offset, jint incx) {
  char * jni_uplo = (char *)(*env)->GetStringUTFChars(env, uplo, JNI_FALSE);
  char * jni_trans = (char *)(*env)->GetStringUTFChars(env, trans, JNI_FALSE);
  char * jni_diag = (char *)(*env)->GetStringUTFChars(env, diag, JNI_FALSE);
  jdouble * jni_a = (jdouble *)a;
  jdouble * jni_x = (jdouble *)x;
cblas_dtbmv(CblasColMajor, getCblasUpLo(jni_uplo), getCblasTrans(jni_trans), getCblasDiag(jni_diag), n, k, jni_a + _a_offset, lda, jni_x + _x_offset, incx);
  (*env)->ReleaseStringUTFChars(env, diag, jni_diag);
  (*env)->ReleaseStringUTFChars(env, trans, jni_trans);
  (*env)->ReleaseStringUTFChars(env, uplo, jni_uplo);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dtbsv (JNIEnv * env, jobject calling_obj, jstring uplo, jstring trans, jstring diag, jint n, jint k, jlong a, jint lda, jlong x, jint incx) {
  char * jni_uplo = (char *)(*env)->GetStringUTFChars(env, uplo, JNI_FALSE);
  char * jni_trans = (char *)(*env)->GetStringUTFChars(env, trans, JNI_FALSE);
  char * jni_diag = (char *)(*env)->GetStringUTFChars(env, diag, JNI_FALSE);
  jdouble * jni_a = (jdouble *)a;
  jdouble * jni_x = (jdouble *)x;
cblas_dtbsv(CblasColMajor, getCblasUpLo(jni_uplo), getCblasTrans(jni_trans), getCblasDiag(jni_diag), n, k, jni_a, lda, jni_x, incx);
  (*env)->ReleaseStringUTFChars(env, diag, jni_diag);
  (*env)->ReleaseStringUTFChars(env, trans, jni_trans);
  (*env)->ReleaseStringUTFChars(env, uplo, jni_uplo);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dtbsv_1offsets (JNIEnv * env, jobject calling_obj, jstring uplo, jstring trans, jstring diag, jint n, jint k, jlong a, jint _a_offset, jint lda, jlong x, jint _x_offset, jint incx) {
  char * jni_uplo = (char *)(*env)->GetStringUTFChars(env, uplo, JNI_FALSE);
  char * jni_trans = (char *)(*env)->GetStringUTFChars(env, trans, JNI_FALSE);
  char * jni_diag = (char *)(*env)->GetStringUTFChars(env, diag, JNI_FALSE);
  jdouble * jni_a = (jdouble *)a;
  jdouble * jni_x = (jdouble *)x;
cblas_dtbsv(CblasColMajor, getCblasUpLo(jni_uplo), getCblasTrans(jni_trans), getCblasDiag(jni_diag), n, k, jni_a + _a_offset, lda, jni_x + _x_offset, incx);
  (*env)->ReleaseStringUTFChars(env, diag, jni_diag);
  (*env)->ReleaseStringUTFChars(env, trans, jni_trans);
  (*env)->ReleaseStringUTFChars(env, uplo, jni_uplo);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dtpmv (JNIEnv * env, jobject calling_obj, jstring uplo, jstring trans, jstring diag, jint n, jlong ap, jlong x, jint incx) {
  char * jni_uplo = (char *)(*env)->GetStringUTFChars(env, uplo, JNI_FALSE);
  char * jni_trans = (char *)(*env)->GetStringUTFChars(env, trans, JNI_FALSE);
  char * jni_diag = (char *)(*env)->GetStringUTFChars(env, diag, JNI_FALSE);
  jdouble * jni_ap = (jdouble *)ap;
  jdouble * jni_x = (jdouble *)x;
cblas_dtpmv(CblasColMajor, getCblasUpLo(jni_uplo), getCblasTrans(jni_trans), getCblasDiag(jni_diag), n, jni_ap, jni_x, incx);
  (*env)->ReleaseStringUTFChars(env, diag, jni_diag);
  (*env)->ReleaseStringUTFChars(env, trans, jni_trans);
  (*env)->ReleaseStringUTFChars(env, uplo, jni_uplo);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dtpmv_1offsets (JNIEnv * env, jobject calling_obj, jstring uplo, jstring trans, jstring diag, jint n, jlong ap, jint _ap_offset, jlong x, jint _x_offset, jint incx) {
  char * jni_uplo = (char *)(*env)->GetStringUTFChars(env, uplo, JNI_FALSE);
  char * jni_trans = (char *)(*env)->GetStringUTFChars(env, trans, JNI_FALSE);
  char * jni_diag = (char *)(*env)->GetStringUTFChars(env, diag, JNI_FALSE);
  jdouble * jni_ap = (jdouble *)ap;
  jdouble * jni_x = (jdouble *)x;
cblas_dtpmv(CblasColMajor, getCblasUpLo(jni_uplo), getCblasTrans(jni_trans), getCblasDiag(jni_diag), n, jni_ap + _ap_offset, jni_x + _x_offset, incx);
  (*env)->ReleaseStringUTFChars(env, diag, jni_diag);
  (*env)->ReleaseStringUTFChars(env, trans, jni_trans);
  (*env)->ReleaseStringUTFChars(env, uplo, jni_uplo);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dtpsv (JNIEnv * env, jobject calling_obj, jstring uplo, jstring trans, jstring diag, jint n, jlong ap, jlong x, jint incx) {
  char * jni_uplo = (char *)(*env)->GetStringUTFChars(env, uplo, JNI_FALSE);
  char * jni_trans = (char *)(*env)->GetStringUTFChars(env, trans, JNI_FALSE);
  char * jni_diag = (char *)(*env)->GetStringUTFChars(env, diag, JNI_FALSE);
  jdouble * jni_ap = (jdouble *)ap;
  jdouble * jni_x = (jdouble *)x;
cblas_dtpsv(CblasColMajor, getCblasUpLo(jni_uplo), getCblasTrans(jni_trans), getCblasDiag(jni_diag), n, jni_ap, jni_x, incx);
  (*env)->ReleaseStringUTFChars(env, diag, jni_diag);
  (*env)->ReleaseStringUTFChars(env, trans, jni_trans);
  (*env)->ReleaseStringUTFChars(env, uplo, jni_uplo);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dtpsv_1offsets (JNIEnv * env, jobject calling_obj, jstring uplo, jstring trans, jstring diag, jint n, jlong ap, jint _ap_offset, jlong x, jint _x_offset, jint incx) {
  char * jni_uplo = (char *)(*env)->GetStringUTFChars(env, uplo, JNI_FALSE);
  char * jni_trans = (char *)(*env)->GetStringUTFChars(env, trans, JNI_FALSE);
  char * jni_diag = (char *)(*env)->GetStringUTFChars(env, diag, JNI_FALSE);
  jdouble * jni_ap = (jdouble *)ap;
  jdouble * jni_x = (jdouble *)x;
cblas_dtpsv(CblasColMajor, getCblasUpLo(jni_uplo), getCblasTrans(jni_trans), getCblasDiag(jni_diag), n, jni_ap + _ap_offset, jni_x + _x_offset, incx);
  (*env)->ReleaseStringUTFChars(env, diag, jni_diag);
  (*env)->ReleaseStringUTFChars(env, trans, jni_trans);
  (*env)->ReleaseStringUTFChars(env, uplo, jni_uplo);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dtrmm (JNIEnv * env, jobject calling_obj, jstring side, jstring uplo, jstring transa, jstring diag, jint m, jint n, jdouble alpha, jlong a, jint lda, jlong b, jint ldb) {
  char * jni_side = (char *)(*env)->GetStringUTFChars(env, side, JNI_FALSE);
  char * jni_uplo = (char *)(*env)->GetStringUTFChars(env, uplo, JNI_FALSE);
  char * jni_transa = (char *)(*env)->GetStringUTFChars(env, transa, JNI_FALSE);
  char * jni_diag = (char *)(*env)->GetStringUTFChars(env, diag, JNI_FALSE);
  jdouble * jni_a = (jdouble *)a;
  jdouble * jni_b = (jdouble *)b;
cblas_dtrmm(CblasColMajor, getCblasSide(jni_side), getCblasUpLo(jni_uplo), getCblasTrans(jni_transa), getCblasDiag(jni_diag), m, n, alpha, jni_a, lda, jni_b, ldb);
  (*env)->ReleaseStringUTFChars(env, diag, jni_diag);
  (*env)->ReleaseStringUTFChars(env, transa, jni_transa);
  (*env)->ReleaseStringUTFChars(env, uplo, jni_uplo);
  (*env)->ReleaseStringUTFChars(env, side, jni_side);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dtrmm_1offsets (JNIEnv * env, jobject calling_obj, jstring side, jstring uplo, jstring transa, jstring diag, jint m, jint n, jdouble alpha, jlong a, jint _a_offset, jint lda, jlong b, jint _b_offset, jint ldb) {
  char * jni_side = (char *)(*env)->GetStringUTFChars(env, side, JNI_FALSE);
  char * jni_uplo = (char *)(*env)->GetStringUTFChars(env, uplo, JNI_FALSE);
  char * jni_transa = (char *)(*env)->GetStringUTFChars(env, transa, JNI_FALSE);
  char * jni_diag = (char *)(*env)->GetStringUTFChars(env, diag, JNI_FALSE);
  jdouble * jni_a = (jdouble *)a;
  jdouble * jni_b = (jdouble *)b;
cblas_dtrmm(CblasColMajor, getCblasSide(jni_side), getCblasUpLo(jni_uplo), getCblasTrans(jni_transa), getCblasDiag(jni_diag), m, n, alpha, jni_a + _a_offset, lda, jni_b + _b_offset, ldb);
  (*env)->ReleaseStringUTFChars(env, diag, jni_diag);
  (*env)->ReleaseStringUTFChars(env, transa, jni_transa);
  (*env)->ReleaseStringUTFChars(env, uplo, jni_uplo);
  (*env)->ReleaseStringUTFChars(env, side, jni_side);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dtrmv (JNIEnv * env, jobject calling_obj, jstring uplo, jstring trans, jstring diag, jint n, jlong a, jint lda, jlong x, jint incx) {
  char * jni_uplo = (char *)(*env)->GetStringUTFChars(env, uplo, JNI_FALSE);
  char * jni_trans = (char *)(*env)->GetStringUTFChars(env, trans, JNI_FALSE);
  char * jni_diag = (char *)(*env)->GetStringUTFChars(env, diag, JNI_FALSE);
  jdouble * jni_a = (jdouble *)a;
  jdouble * jni_x = (jdouble *)x;
cblas_dtrmv(CblasColMajor, getCblasUpLo(jni_uplo), getCblasTrans(jni_trans), getCblasDiag(jni_diag), n, jni_a, lda, jni_x, incx);
  (*env)->ReleaseStringUTFChars(env, diag, jni_diag);
  (*env)->ReleaseStringUTFChars(env, trans, jni_trans);
  (*env)->ReleaseStringUTFChars(env, uplo, jni_uplo);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dtrmv_1offsets (JNIEnv * env, jobject calling_obj, jstring uplo, jstring trans, jstring diag, jint n, jlong a, jint _a_offset, jint lda, jlong x, jint _x_offset, jint incx) {
  char * jni_uplo = (char *)(*env)->GetStringUTFChars(env, uplo, JNI_FALSE);
  char * jni_trans = (char *)(*env)->GetStringUTFChars(env, trans, JNI_FALSE);
  char * jni_diag = (char *)(*env)->GetStringUTFChars(env, diag, JNI_FALSE);
  jdouble * jni_a = (jdouble *)a;
  jdouble * jni_x = (jdouble *)x;
cblas_dtrmv(CblasColMajor, getCblasUpLo(jni_uplo), getCblasTrans(jni_trans), getCblasDiag(jni_diag), n, jni_a + _a_offset, lda, jni_x + _x_offset, incx);
  (*env)->ReleaseStringUTFChars(env, diag, jni_diag);
  (*env)->ReleaseStringUTFChars(env, trans, jni_trans);
  (*env)->ReleaseStringUTFChars(env, uplo, jni_uplo);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dtrsm (JNIEnv * env, jobject calling_obj, jstring side, jstring uplo, jstring transa, jstring diag, jint m, jint n, jdouble alpha, jlong a, jint lda, jlong b, jint ldb) {
  char * jni_side = (char *)(*env)->GetStringUTFChars(env, side, JNI_FALSE);
  char * jni_uplo = (char *)(*env)->GetStringUTFChars(env, uplo, JNI_FALSE);
  char * jni_transa = (char *)(*env)->GetStringUTFChars(env, transa, JNI_FALSE);
  char * jni_diag = (char *)(*env)->GetStringUTFChars(env, diag, JNI_FALSE);
  jdouble * jni_a = (jdouble *)a;
  jdouble * jni_b = (jdouble *)b;
cblas_dtrsm(CblasColMajor, getCblasSide(jni_side), getCblasUpLo(jni_uplo), getCblasTrans(jni_transa), getCblasDiag(jni_diag), m, n, alpha, jni_a, lda, jni_b, ldb);
  (*env)->ReleaseStringUTFChars(env, diag, jni_diag);
  (*env)->ReleaseStringUTFChars(env, transa, jni_transa);
  (*env)->ReleaseStringUTFChars(env, uplo, jni_uplo);
  (*env)->ReleaseStringUTFChars(env, side, jni_side);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dtrsm_1offsets (JNIEnv * env, jobject calling_obj, jstring side, jstring uplo, jstring transa, jstring diag, jint m, jint n, jdouble alpha, jlong a, jint _a_offset, jint lda, jlong b, jint _b_offset, jint ldb) {
  char * jni_side = (char *)(*env)->GetStringUTFChars(env, side, JNI_FALSE);
  char * jni_uplo = (char *)(*env)->GetStringUTFChars(env, uplo, JNI_FALSE);
  char * jni_transa = (char *)(*env)->GetStringUTFChars(env, transa, JNI_FALSE);
  char * jni_diag = (char *)(*env)->GetStringUTFChars(env, diag, JNI_FALSE);
  jdouble * jni_a = (jdouble *)a;
  jdouble * jni_b = (jdouble *)b;
cblas_dtrsm(CblasColMajor, getCblasSide(jni_side), getCblasUpLo(jni_uplo), getCblasTrans(jni_transa), getCblasDiag(jni_diag), m, n, alpha, jni_a + _a_offset, lda, jni_b + _b_offset, ldb);
  (*env)->ReleaseStringUTFChars(env, diag, jni_diag);
  (*env)->ReleaseStringUTFChars(env, transa, jni_transa);
  (*env)->ReleaseStringUTFChars(env, uplo, jni_uplo);
  (*env)->ReleaseStringUTFChars(env, side, jni_side);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dtrsv (JNIEnv * env, jobject calling_obj, jstring uplo, jstring trans, jstring diag, jint n, jlong a, jint lda, jlong x, jint incx) {
  char * jni_uplo = (char *)(*env)->GetStringUTFChars(env, uplo, JNI_FALSE);
  char * jni_trans = (char *)(*env)->GetStringUTFChars(env, trans, JNI_FALSE);
  char * jni_diag = (char *)(*env)->GetStringUTFChars(env, diag, JNI_FALSE);
  jdouble * jni_a = (jdouble *)a;
  jdouble * jni_x = (jdouble *)x;
cblas_dtrsv(CblasColMajor, getCblasUpLo(jni_uplo), getCblasTrans(jni_trans), getCblasDiag(jni_diag), n, jni_a, lda, jni_x, incx);
  (*env)->ReleaseStringUTFChars(env, diag, jni_diag);
  (*env)->ReleaseStringUTFChars(env, trans, jni_trans);
  (*env)->ReleaseStringUTFChars(env, uplo, jni_uplo);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_dtrsv_1offsets (JNIEnv * env, jobject calling_obj, jstring uplo, jstring trans, jstring diag, jint n, jlong a, jint _a_offset, jint lda, jlong x, jint _x_offset, jint incx) {
  char * jni_uplo = (char *)(*env)->GetStringUTFChars(env, uplo, JNI_FALSE);
  char * jni_trans = (char *)(*env)->GetStringUTFChars(env, trans, JNI_FALSE);
  char * jni_diag = (char *)(*env)->GetStringUTFChars(env, diag, JNI_FALSE);
  jdouble * jni_a = (jdouble *)a;
  jdouble * jni_x = (jdouble *)x;
cblas_dtrsv(CblasColMajor, getCblasUpLo(jni_uplo), getCblasTrans(jni_trans), getCblasDiag(jni_diag), n, jni_a + _a_offset, lda, jni_x + _x_offset, incx);
  (*env)->ReleaseStringUTFChars(env, diag, jni_diag);
  (*env)->ReleaseStringUTFChars(env, trans, jni_trans);
  (*env)->ReleaseStringUTFChars(env, uplo, jni_uplo);
}

JNIEXPORT jint JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_idamax (JNIEnv * env, jobject calling_obj, jint n, jlong dx, jint incx) {
  jdouble * jni_dx = (jdouble *)dx;
  jint returnValue = cblas_idamax(n, jni_dx, incx);
  return returnValue;
}

JNIEXPORT jint JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_idamax_1offsets (JNIEnv * env, jobject calling_obj, jint n, jlong dx, jint _dx_offset, jint incx) {
  jdouble * jni_dx = (jdouble *)dx;
  jint returnValue = cblas_idamax(n, jni_dx + _dx_offset, incx);
  return returnValue;
}

JNIEXPORT jint JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_isamax (JNIEnv * env, jobject calling_obj, jint n, jfloatArray sx, jint incx) {
  jfloat * jni_sx = NULL;
  if (sx != NULL) {
    jni_sx = (*env)->GetPrimitiveArrayCritical(env, sx, JNI_FALSE);
    check_memory(env, jni_sx);
  }
  jint returnValue = cblas_isamax(n, jni_sx, incx);
  if (sx != NULL) {
    (*env)->ReleasePrimitiveArrayCritical(env, sx, jni_sx, 0);
  }
  return returnValue;
}

JNIEXPORT jint JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_isamax_1offsets (JNIEnv * env, jobject calling_obj, jint n, jfloatArray sx, jint _sx_offset, jint incx) {
  jfloat * jni_sx = NULL;
  if (sx != NULL) {
    jni_sx = (*env)->GetPrimitiveArrayCritical(env, sx, JNI_FALSE);
    check_memory(env, jni_sx);
  }
  jint returnValue = cblas_isamax(n, jni_sx + _sx_offset, incx);
  if (sx != NULL) {
    (*env)->ReleasePrimitiveArrayCritical(env, sx, jni_sx, 0);
  }
  return returnValue;
}

JNIEXPORT jfloat JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_sasum (JNIEnv * env, jobject calling_obj, jint n, jfloatArray sx, jint incx) {
  jfloat * jni_sx = NULL;
  if (sx != NULL) {
    jni_sx = (*env)->GetPrimitiveArrayCritical(env, sx, JNI_FALSE);
    check_memory(env, jni_sx);
  }
  jfloat returnValue = cblas_sasum(n, jni_sx, incx);
  if (sx != NULL) {
    (*env)->ReleasePrimitiveArrayCritical(env, sx, jni_sx, 0);
  }
  return returnValue;
}

JNIEXPORT jfloat JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_sasum_1offsets (JNIEnv * env, jobject calling_obj, jint n, jfloatArray sx, jint _sx_offset, jint incx) {
  jfloat * jni_sx = NULL;
  if (sx != NULL) {
    jni_sx = (*env)->GetPrimitiveArrayCritical(env, sx, JNI_FALSE);
    check_memory(env, jni_sx);
  }
  jfloat returnValue = cblas_sasum(n, jni_sx + _sx_offset, incx);
  if (sx != NULL) {
    (*env)->ReleasePrimitiveArrayCritical(env, sx, jni_sx, 0);
  }
  return returnValue;
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_saxpy (JNIEnv * env, jobject calling_obj, jint n, jfloat sa, jfloatArray sx, jint incx, jfloatArray sy, jint incy) {
  jfloat * jni_sx = NULL;
  if (sx != NULL) {
    jni_sx = (*env)->GetPrimitiveArrayCritical(env, sx, JNI_FALSE);
    check_memory(env, jni_sx);
  }
  jfloat * jni_sy = NULL;
  if (sy != NULL) {
    jni_sy = (*env)->GetPrimitiveArrayCritical(env, sy, JNI_FALSE);
    check_memory(env, jni_sy);
  }
cblas_saxpy(n, sa, jni_sx, incx, jni_sy, incy);
  if (sy != NULL) {
    (*env)->ReleasePrimitiveArrayCritical(env, sy, jni_sy, 0);
  }
  if (sx != NULL) {
    (*env)->ReleasePrimitiveArrayCritical(env, sx, jni_sx, 0);
  }
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_saxpy_1offsets (JNIEnv * env, jobject calling_obj, jint n, jfloat sa, jfloatArray sx, jint _sx_offset, jint incx, jfloatArray sy, jint _sy_offset, jint incy) {
  jfloat * jni_sx = NULL;
  if (sx != NULL) {
    jni_sx = (*env)->GetPrimitiveArrayCritical(env, sx, JNI_FALSE);
    check_memory(env, jni_sx);
  }
  jfloat * jni_sy = NULL;
  if (sy != NULL) {
    jni_sy = (*env)->GetPrimitiveArrayCritical(env, sy, JNI_FALSE);
    check_memory(env, jni_sy);
  }
cblas_saxpy(n, sa, jni_sx + _sx_offset, incx, jni_sy + _sy_offset, incy);
  if (sy != NULL) {
    (*env)->ReleasePrimitiveArrayCritical(env, sy, jni_sy, 0);
  }
  if (sx != NULL) {
    (*env)->ReleasePrimitiveArrayCritical(env, sx, jni_sx, 0);
  }
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_scopy (JNIEnv * env, jobject calling_obj, jint n, jfloatArray sx, jint incx, jfloatArray sy, jint incy) {
  jfloat * jni_sx = NULL;
  if (sx != NULL) {
    jni_sx = (*env)->GetPrimitiveArrayCritical(env, sx, JNI_FALSE);
    check_memory(env, jni_sx);
  }
  jfloat * jni_sy = NULL;
  if (sy != NULL) {
    jni_sy = (*env)->GetPrimitiveArrayCritical(env, sy, JNI_FALSE);
    check_memory(env, jni_sy);
  }
cblas_scopy(n, jni_sx, incx, jni_sy, incy);
  if (sy != NULL) {
    (*env)->ReleasePrimitiveArrayCritical(env, sy, jni_sy, 0);
  }
  if (sx != NULL) {
    (*env)->ReleasePrimitiveArrayCritical(env, sx, jni_sx, 0);
  }
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_scopy_1offsets (JNIEnv * env, jobject calling_obj, jint n, jfloatArray sx, jint _sx_offset, jint incx, jfloatArray sy, jint _sy_offset, jint incy) {
  jfloat * jni_sx = NULL;
  if (sx != NULL) {
    jni_sx = (*env)->GetPrimitiveArrayCritical(env, sx, JNI_FALSE);
    check_memory(env, jni_sx);
  }
  jfloat * jni_sy = NULL;
  if (sy != NULL) {
    jni_sy = (*env)->GetPrimitiveArrayCritical(env, sy, JNI_FALSE);
    check_memory(env, jni_sy);
  }
cblas_scopy(n, jni_sx + _sx_offset, incx, jni_sy + _sy_offset, incy);
  if (sy != NULL) {
    (*env)->ReleasePrimitiveArrayCritical(env, sy, jni_sy, 0);
  }
  if (sx != NULL) {
    (*env)->ReleasePrimitiveArrayCritical(env, sx, jni_sx, 0);
  }
}

JNIEXPORT jfloat JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_sdot (JNIEnv * env, jobject calling_obj, jint n, jfloatArray sx, jint incx, jfloatArray sy, jint incy) {
  jfloat * jni_sx = NULL;
  if (sx != NULL) {
    jni_sx = (*env)->GetPrimitiveArrayCritical(env, sx, JNI_FALSE);
    check_memory(env, jni_sx);
  }
  jfloat * jni_sy = NULL;
  if (sy != NULL) {
    jni_sy = (*env)->GetPrimitiveArrayCritical(env, sy, JNI_FALSE);
    check_memory(env, jni_sy);
  }
  jfloat returnValue = cblas_sdot(n, jni_sx, incx, jni_sy, incy);
  if (sy != NULL) {
    (*env)->ReleasePrimitiveArrayCritical(env, sy, jni_sy, 0);
  }
  if (sx != NULL) {
    (*env)->ReleasePrimitiveArrayCritical(env, sx, jni_sx, 0);
  }
  return returnValue;
}

JNIEXPORT jfloat JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_sdot_1offsets (JNIEnv * env, jobject calling_obj, jint n, jfloatArray sx, jint _sx_offset, jint incx, jfloatArray sy, jint _sy_offset, jint incy) {
  jfloat * jni_sx = NULL;
  if (sx != NULL) {
    jni_sx = (*env)->GetPrimitiveArrayCritical(env, sx, JNI_FALSE);
    check_memory(env, jni_sx);
  }
  jfloat * jni_sy = NULL;
  if (sy != NULL) {
    jni_sy = (*env)->GetPrimitiveArrayCritical(env, sy, JNI_FALSE);
    check_memory(env, jni_sy);
  }
  jfloat returnValue = cblas_sdot(n, jni_sx + _sx_offset, incx, jni_sy + _sy_offset, incy);
  if (sy != NULL) {
    (*env)->ReleasePrimitiveArrayCritical(env, sy, jni_sy, 0);
  }
  if (sx != NULL) {
    (*env)->ReleasePrimitiveArrayCritical(env, sx, jni_sx, 0);
  }
  return returnValue;
}

JNIEXPORT jfloat JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_sdsdot (JNIEnv * env, jobject calling_obj, jint n, jfloat sb, jfloatArray sx, jint incx, jfloatArray sy, jint incy) {
  jfloat * jni_sx = NULL;
  if (sx != NULL) {
    jni_sx = (*env)->GetPrimitiveArrayCritical(env, sx, JNI_FALSE);
    check_memory(env, jni_sx);
  }
  jfloat * jni_sy = NULL;
  if (sy != NULL) {
    jni_sy = (*env)->GetPrimitiveArrayCritical(env, sy, JNI_FALSE);
    check_memory(env, jni_sy);
  }
  jfloat returnValue = cblas_sdsdot(n, sb, jni_sx, incx, jni_sy, incy);
  if (sy != NULL) {
    (*env)->ReleasePrimitiveArrayCritical(env, sy, jni_sy, 0);
  }
  if (sx != NULL) {
    (*env)->ReleasePrimitiveArrayCritical(env, sx, jni_sx, 0);
  }
  return returnValue;
}

JNIEXPORT jfloat JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_sdsdot_1offsets (JNIEnv * env, jobject calling_obj, jint n, jfloat sb, jfloatArray sx, jint _sx_offset, jint incx, jfloatArray sy, jint _sy_offset, jint incy) {
  jfloat * jni_sx = NULL;
  if (sx != NULL) {
    jni_sx = (*env)->GetPrimitiveArrayCritical(env, sx, JNI_FALSE);
    check_memory(env, jni_sx);
  }
  jfloat * jni_sy = NULL;
  if (sy != NULL) {
    jni_sy = (*env)->GetPrimitiveArrayCritical(env, sy, JNI_FALSE);
    check_memory(env, jni_sy);
  }
  jfloat returnValue = cblas_sdsdot(n, sb, jni_sx + _sx_offset, incx, jni_sy + _sy_offset, incy);
  if (sy != NULL) {
    (*env)->ReleasePrimitiveArrayCritical(env, sy, jni_sy, 0);
  }
  if (sx != NULL) {
    (*env)->ReleasePrimitiveArrayCritical(env, sx, jni_sx, 0);
  }
  return returnValue;
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_sgbmv (JNIEnv * env, jobject calling_obj, jstring trans, jint m, jint n, jint kl, jint ku, jfloat alpha, jfloatArray a, jint lda, jfloatArray x, jint incx, jfloat beta, jfloatArray y, jint incy) {
  char * jni_trans = (char *)(*env)->GetStringUTFChars(env, trans, JNI_FALSE);
  jfloat * jni_a = NULL;
  if (a != NULL) {
    jni_a = (*env)->GetPrimitiveArrayCritical(env, a, JNI_FALSE);
    check_memory(env, jni_a);
  }
  jfloat * jni_x = NULL;
  if (x != NULL) {
    jni_x = (*env)->GetPrimitiveArrayCritical(env, x, JNI_FALSE);
    check_memory(env, jni_x);
  }
  jfloat * jni_y = NULL;
  if (y != NULL) {
    jni_y = (*env)->GetPrimitiveArrayCritical(env, y, JNI_FALSE);
    check_memory(env, jni_y);
  }
cblas_sgbmv(CblasColMajor, getCblasTrans(jni_trans), m, n, kl, ku, alpha, jni_a, lda, jni_x, incx, beta, jni_y, incy);
  (*env)->ReleaseStringUTFChars(env, trans, jni_trans);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_sgbmv_1offsets (JNIEnv * env, jobject calling_obj, jstring trans, jint m, jint n, jint kl, jint ku, jfloat alpha, jfloatArray a, jint _a_offset, jint lda, jfloatArray x, jint _x_offset, jint incx, jfloat beta, jfloatArray y, jint _y_offset, jint incy) {
  char * jni_trans = (char *)(*env)->GetStringUTFChars(env, trans, JNI_FALSE);
  jfloat * jni_a = NULL;
  if (a != NULL) {
    jni_a = (*env)->GetPrimitiveArrayCritical(env, a, JNI_FALSE);
    check_memory(env, jni_a);
  }
  jfloat * jni_x = NULL;
  if (x != NULL) {
    jni_x = (*env)->GetPrimitiveArrayCritical(env, x, JNI_FALSE);
    check_memory(env, jni_x);
  }
  jfloat * jni_y = NULL;
  if (y != NULL) {
    jni_y = (*env)->GetPrimitiveArrayCritical(env, y, JNI_FALSE);
    check_memory(env, jni_y);
  }
cblas_sgbmv(CblasColMajor, getCblasTrans(jni_trans), m, n, kl, ku, alpha, jni_a + _a_offset, lda, jni_x + _x_offset, incx, beta, jni_y + _y_offset, incy);
  (*env)->ReleaseStringUTFChars(env, trans, jni_trans);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_sgemm (JNIEnv * env, jobject calling_obj, jstring transa, jstring transb, jint m, jint n, jint k, jfloat alpha, jfloatArray a, jint lda, jfloatArray b, jint ldb, jfloat beta, jfloatArray c, jint Ldc) {
  char * jni_transa = (char *)(*env)->GetStringUTFChars(env, transa, JNI_FALSE);
  char * jni_transb = (char *)(*env)->GetStringUTFChars(env, transb, JNI_FALSE);
  jfloat * jni_a = NULL;
  if (a != NULL) {
    jni_a = (*env)->GetPrimitiveArrayCritical(env, a, JNI_FALSE);
    check_memory(env, jni_a);
  }
  jfloat * jni_b = NULL;
  if (b != NULL) {
    jni_b = (*env)->GetPrimitiveArrayCritical(env, b, JNI_FALSE);
    check_memory(env, jni_b);
  }
  jfloat * jni_c = NULL;
  if (c != NULL) {
    jni_c = (*env)->GetPrimitiveArrayCritical(env, c, JNI_FALSE);
    check_memory(env, jni_c);
  }
cblas_sgemm(CblasColMajor, getCblasTrans(jni_transa), getCblasTrans(jni_transb), m, n, k, alpha, jni_a, lda, jni_b, ldb, beta, jni_c, Ldc);
  (*env)->ReleaseStringUTFChars(env, transb, jni_transb);
  (*env)->ReleaseStringUTFChars(env, transa, jni_transa);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_sgemm_1offsets (JNIEnv * env, jobject calling_obj, jstring transa, jstring transb, jint m, jint n, jint k, jfloat alpha, jfloatArray a, jint _a_offset, jint lda, jfloatArray b, jint _b_offset, jint ldb, jfloat beta, jfloatArray c, jint _c_offset, jint Ldc) {
  char * jni_transa = (char *)(*env)->GetStringUTFChars(env, transa, JNI_FALSE);
  char * jni_transb = (char *)(*env)->GetStringUTFChars(env, transb, JNI_FALSE);
  jfloat * jni_a = NULL;
  if (a != NULL) {
    jni_a = (*env)->GetPrimitiveArrayCritical(env, a, JNI_FALSE);
    check_memory(env, jni_a);
  }
  jfloat * jni_b = NULL;
  if (b != NULL) {
    jni_b = (*env)->GetPrimitiveArrayCritical(env, b, JNI_FALSE);
    check_memory(env, jni_b);
  }
  jfloat * jni_c = NULL;
  if (c != NULL) {
    jni_c = (*env)->GetPrimitiveArrayCritical(env, c, JNI_FALSE);
    check_memory(env, jni_c);
  }
cblas_sgemm(CblasColMajor, getCblasTrans(jni_transa), getCblasTrans(jni_transb), m, n, k, alpha, jni_a + _a_offset, lda, jni_b + _b_offset, ldb, beta, jni_c + _c_offset, Ldc);
  (*env)->ReleaseStringUTFChars(env, transb, jni_transb);
  (*env)->ReleaseStringUTFChars(env, transa, jni_transa);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_sgemv (JNIEnv * env, jobject calling_obj, jstring trans, jint m, jint n, jfloat alpha, jfloatArray a, jint lda, jfloatArray x, jint incx, jfloat beta, jfloatArray y, jint incy) {
  char * jni_trans = (char *)(*env)->GetStringUTFChars(env, trans, JNI_FALSE);
  jfloat * jni_a = NULL;
  if (a != NULL) {
    jni_a = (*env)->GetPrimitiveArrayCritical(env, a, JNI_FALSE);
    check_memory(env, jni_a);
  }
  jfloat * jni_x = NULL;
  if (x != NULL) {
    jni_x = (*env)->GetPrimitiveArrayCritical(env, x, JNI_FALSE);
    check_memory(env, jni_x);
  }
  jfloat * jni_y = NULL;
  if (y != NULL) {
    jni_y = (*env)->GetPrimitiveArrayCritical(env, y, JNI_FALSE);
    check_memory(env, jni_y);
  }
cblas_sgemv(CblasColMajor, getCblasTrans(jni_trans), m, n, alpha, jni_a, lda, jni_x, incx, beta, jni_y, incy);
  (*env)->ReleaseStringUTFChars(env, trans, jni_trans);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_sgemv_1offsets (JNIEnv * env, jobject calling_obj, jstring trans, jint m, jint n, jfloat alpha, jfloatArray a, jint _a_offset, jint lda, jfloatArray x, jint _x_offset, jint incx, jfloat beta, jfloatArray y, jint _y_offset, jint incy) {
  char * jni_trans = (char *)(*env)->GetStringUTFChars(env, trans, JNI_FALSE);
  jfloat * jni_a = NULL;
  if (a != NULL) {
    jni_a = (*env)->GetPrimitiveArrayCritical(env, a, JNI_FALSE);
    check_memory(env, jni_a);
  }
  jfloat * jni_x = NULL;
  if (x != NULL) {
    jni_x = (*env)->GetPrimitiveArrayCritical(env, x, JNI_FALSE);
    check_memory(env, jni_x);
  }
  jfloat * jni_y = NULL;
  if (y != NULL) {
    jni_y = (*env)->GetPrimitiveArrayCritical(env, y, JNI_FALSE);
    check_memory(env, jni_y);
  }
cblas_sgemv(CblasColMajor, getCblasTrans(jni_trans), m, n, alpha, jni_a + _a_offset, lda, jni_x + _x_offset, incx, beta, jni_y + _y_offset, incy);
  (*env)->ReleaseStringUTFChars(env, trans, jni_trans);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_sger (JNIEnv * env, jobject calling_obj, jint m, jint n, jfloat alpha, jfloatArray x, jint incx, jfloatArray y, jint incy, jfloatArray a, jint lda) {
  jfloat * jni_x = NULL;
  if (x != NULL) {
    jni_x = (*env)->GetPrimitiveArrayCritical(env, x, JNI_FALSE);
    check_memory(env, jni_x);
  }
  jfloat * jni_y = NULL;
  if (y != NULL) {
    jni_y = (*env)->GetPrimitiveArrayCritical(env, y, JNI_FALSE);
    check_memory(env, jni_y);
  }
  jfloat * jni_a = NULL;
  if (a != NULL) {
    jni_a = (*env)->GetPrimitiveArrayCritical(env, a, JNI_FALSE);
    check_memory(env, jni_a);
  }
cblas_sger(CblasColMajor, m, n, alpha, jni_x, incx, jni_y, incy, jni_a, lda);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_sger_1offsets (JNIEnv * env, jobject calling_obj, jint m, jint n, jfloat alpha, jfloatArray x, jint _x_offset, jint incx, jfloatArray y, jint _y_offset, jint incy, jfloatArray a, jint _a_offset, jint lda) {
  jfloat * jni_x = NULL;
  if (x != NULL) {
    jni_x = (*env)->GetPrimitiveArrayCritical(env, x, JNI_FALSE);
    check_memory(env, jni_x);
  }
  jfloat * jni_y = NULL;
  if (y != NULL) {
    jni_y = (*env)->GetPrimitiveArrayCritical(env, y, JNI_FALSE);
    check_memory(env, jni_y);
  }
  jfloat * jni_a = NULL;
  if (a != NULL) {
    jni_a = (*env)->GetPrimitiveArrayCritical(env, a, JNI_FALSE);
    check_memory(env, jni_a);
  }
cblas_sger(CblasColMajor, m, n, alpha, jni_x + _x_offset, incx, jni_y + _y_offset, incy, jni_a + _a_offset, lda);
}

JNIEXPORT jfloat JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_snrm2 (JNIEnv * env, jobject calling_obj, jint n, jfloatArray x, jint incx) {
  jfloat * jni_x = NULL;
  if (x != NULL) {
    jni_x = (*env)->GetPrimitiveArrayCritical(env, x, JNI_FALSE);
    check_memory(env, jni_x);
  }
  jfloat returnValue = cblas_snrm2(n, jni_x, incx);
  return returnValue;
}

JNIEXPORT jfloat JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_snrm2_1offsets (JNIEnv * env, jobject calling_obj, jint n, jfloatArray x, jint _x_offset, jint incx) {
  jfloat * jni_x = NULL;
  if (x != NULL) {
    jni_x = (*env)->GetPrimitiveArrayCritical(env, x, JNI_FALSE);
    check_memory(env, jni_x);
  }
  jfloat returnValue = cblas_snrm2(n, jni_x + _x_offset, incx);
  return returnValue;
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_srot (JNIEnv * env, jobject calling_obj, jint n, jfloatArray sx, jint incx, jfloatArray sy, jint incy, jfloat c, jfloat s) {
  jfloat * jni_sx = NULL;
  if (sx != NULL) {
    jni_sx = (*env)->GetPrimitiveArrayCritical(env, sx, JNI_FALSE);
    check_memory(env, jni_sx);
  }
  jfloat * jni_sy = NULL;
  if (sy != NULL) {
    jni_sy = (*env)->GetPrimitiveArrayCritical(env, sy, JNI_FALSE);
    check_memory(env, jni_sy);
  }
cblas_srot(n, jni_sx, incx, jni_sy, incy, c, s);
  if (sy != NULL) {
    (*env)->ReleasePrimitiveArrayCritical(env, sy, jni_sy, 0);
  }
  if (sx != NULL) {
    (*env)->ReleasePrimitiveArrayCritical(env, sx, jni_sx, 0);
  }
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_srot_1offsets (JNIEnv * env, jobject calling_obj, jint n, jfloatArray sx, jint _sx_offset, jint incx, jfloatArray sy, jint _sy_offset, jint incy, jfloat c, jfloat s) {
  jfloat * jni_sx = NULL;
  if (sx != NULL) {
    jni_sx = (*env)->GetPrimitiveArrayCritical(env, sx, JNI_FALSE);
    check_memory(env, jni_sx);
  }
  jfloat * jni_sy = NULL;
  if (sy != NULL) {
    jni_sy = (*env)->GetPrimitiveArrayCritical(env, sy, JNI_FALSE);
    check_memory(env, jni_sy);
  }
cblas_srot(n, jni_sx + _sx_offset, incx, jni_sy + _sy_offset, incy, c, s);
  if (sy != NULL) {
    (*env)->ReleasePrimitiveArrayCritical(env, sy, jni_sy, 0);
  }
  if (sx != NULL) {
    (*env)->ReleasePrimitiveArrayCritical(env, sx, jni_sx, 0);
  }
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_srotg (JNIEnv * env, jobject calling_obj, jfloatw sa, jfloatw sb, jfloatw c, jfloatw s) {
  jclass jni_saClass = (*env)->GetObjectClass(env, sa);
  jfieldID jni_saId = (*env)->GetFieldID(env, jni_saClass, "val", "F");
  jfloat jni_sa = (*env)->GetFloatField(env, sa, jni_saId);
  jclass jni_sbClass = (*env)->GetObjectClass(env, sb);
  jfieldID jni_sbId = (*env)->GetFieldID(env, jni_sbClass, "val", "F");
  jfloat jni_sb = (*env)->GetFloatField(env, sb, jni_sbId);
  jclass jni_cClass = (*env)->GetObjectClass(env, c);
  jfieldID jni_cId = (*env)->GetFieldID(env, jni_cClass, "val", "F");
  jfloat jni_c = (*env)->GetFloatField(env, c, jni_cId);
  jclass jni_sClass = (*env)->GetObjectClass(env, s);
  jfieldID jni_sId = (*env)->GetFieldID(env, jni_sClass, "val", "F");
  jfloat jni_s = (*env)->GetFloatField(env, s, jni_sId);
cblas_srotg(&jni_sa, &jni_sb, &jni_c, &jni_s);
  (*env)->SetFloatField(env, s, jni_sId, jni_s);
  (*env)->SetFloatField(env, c, jni_cId, jni_c);
  (*env)->SetFloatField(env, sb, jni_sbId, jni_sb);
  (*env)->SetFloatField(env, sa, jni_saId, jni_sa);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_srotm (JNIEnv * env, jobject calling_obj, jint n, jfloatArray sx, jint incx, jfloatArray sy, jint incy, jfloatArray sparam) {
  jfloat * jni_sx = NULL;
  if (sx != NULL) {
    jni_sx = (*env)->GetPrimitiveArrayCritical(env, sx, JNI_FALSE);
    check_memory(env, jni_sx);
  }
  jfloat * jni_sy = NULL;
  if (sy != NULL) {
    jni_sy = (*env)->GetPrimitiveArrayCritical(env, sy, JNI_FALSE);
    check_memory(env, jni_sy);
  }
  jfloat * jni_sparam = NULL;
  if (sparam != NULL) {
    jni_sparam = (*env)->GetPrimitiveArrayCritical(env, sparam, JNI_FALSE);
    check_memory(env, jni_sparam);
  }
cblas_srotm(n, jni_sx, incx, jni_sy, incy, jni_sparam);
  if (sparam != NULL) {
    (*env)->ReleasePrimitiveArrayCritical(env, sparam, jni_sparam, 0);
  }
  if (sy != NULL) {
    (*env)->ReleasePrimitiveArrayCritical(env, sy, jni_sy, 0);
  }
  if (sx != NULL) {
    (*env)->ReleasePrimitiveArrayCritical(env, sx, jni_sx, 0);
  }
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_srotm_1offsets (JNIEnv * env, jobject calling_obj, jint n, jfloatArray sx, jint _sx_offset, jint incx, jfloatArray sy, jint _sy_offset, jint incy, jfloatArray sparam, jint _sparam_offset) {
  jfloat * jni_sx = NULL;
  if (sx != NULL) {
    jni_sx = (*env)->GetPrimitiveArrayCritical(env, sx, JNI_FALSE);
    check_memory(env, jni_sx);
  }
  jfloat * jni_sy = NULL;
  if (sy != NULL) {
    jni_sy = (*env)->GetPrimitiveArrayCritical(env, sy, JNI_FALSE);
    check_memory(env, jni_sy);
  }
  jfloat * jni_sparam = NULL;
  if (sparam != NULL) {
    jni_sparam = (*env)->GetPrimitiveArrayCritical(env, sparam, JNI_FALSE);
    check_memory(env, jni_sparam);
  }
cblas_srotm(n, jni_sx + _sx_offset, incx, jni_sy + _sy_offset, incy, jni_sparam + _sparam_offset);
  if (sparam != NULL) {
    (*env)->ReleasePrimitiveArrayCritical(env, sparam, jni_sparam, 0);
  }
  if (sy != NULL) {
    (*env)->ReleasePrimitiveArrayCritical(env, sy, jni_sy, 0);
  }
  if (sx != NULL) {
    (*env)->ReleasePrimitiveArrayCritical(env, sx, jni_sx, 0);
  }
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_srotmg (JNIEnv * env, jobject calling_obj, jfloatw sd1, jfloatw sd2, jfloatw sx1, jfloat sy1, jfloatArray sparam) {
  jclass jni_sd1Class = (*env)->GetObjectClass(env, sd1);
  jfieldID jni_sd1Id = (*env)->GetFieldID(env, jni_sd1Class, "val", "F");
  jfloat jni_sd1 = (*env)->GetFloatField(env, sd1, jni_sd1Id);
  jclass jni_sd2Class = (*env)->GetObjectClass(env, sd2);
  jfieldID jni_sd2Id = (*env)->GetFieldID(env, jni_sd2Class, "val", "F");
  jfloat jni_sd2 = (*env)->GetFloatField(env, sd2, jni_sd2Id);
  jclass jni_sx1Class = (*env)->GetObjectClass(env, sx1);
  jfieldID jni_sx1Id = (*env)->GetFieldID(env, jni_sx1Class, "val", "F");
  jfloat jni_sx1 = (*env)->GetFloatField(env, sx1, jni_sx1Id);
  jfloat * jni_sparam = NULL;
  if (sparam != NULL) {
    jni_sparam = (*env)->GetPrimitiveArrayCritical(env, sparam, JNI_FALSE);
    check_memory(env, jni_sparam);
  }
cblas_srotmg(&jni_sd1, &jni_sd2, &jni_sx1, sy1, jni_sparam);
  if (sparam != NULL) {
    (*env)->ReleasePrimitiveArrayCritical(env, sparam, jni_sparam, 0);
  }
  (*env)->SetFloatField(env, sx1, jni_sx1Id, jni_sx1);
  (*env)->SetFloatField(env, sd2, jni_sd2Id, jni_sd2);
  (*env)->SetFloatField(env, sd1, jni_sd1Id, jni_sd1);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_srotmg_1offsets (JNIEnv * env, jobject calling_obj, jfloatw sd1, jfloatw sd2, jfloatw sx1, jfloat sy1, jfloatArray sparam, jint _sparam_offset) {
  jclass jni_sd1Class = (*env)->GetObjectClass(env, sd1);
  jfieldID jni_sd1Id = (*env)->GetFieldID(env, jni_sd1Class, "val", "F");
  jfloat jni_sd1 = (*env)->GetFloatField(env, sd1, jni_sd1Id);
  jclass jni_sd2Class = (*env)->GetObjectClass(env, sd2);
  jfieldID jni_sd2Id = (*env)->GetFieldID(env, jni_sd2Class, "val", "F");
  jfloat jni_sd2 = (*env)->GetFloatField(env, sd2, jni_sd2Id);
  jclass jni_sx1Class = (*env)->GetObjectClass(env, sx1);
  jfieldID jni_sx1Id = (*env)->GetFieldID(env, jni_sx1Class, "val", "F");
  jfloat jni_sx1 = (*env)->GetFloatField(env, sx1, jni_sx1Id);
  jfloat * jni_sparam = NULL;
  if (sparam != NULL) {
    jni_sparam = (*env)->GetPrimitiveArrayCritical(env, sparam, JNI_FALSE);
    check_memory(env, jni_sparam);
  }
cblas_srotmg(&jni_sd1, &jni_sd2, &jni_sx1, sy1, jni_sparam + _sparam_offset);
  if (sparam != NULL) {
    (*env)->ReleasePrimitiveArrayCritical(env, sparam, jni_sparam, 0);
  }
  (*env)->SetFloatField(env, sx1, jni_sx1Id, jni_sx1);
  (*env)->SetFloatField(env, sd2, jni_sd2Id, jni_sd2);
  (*env)->SetFloatField(env, sd1, jni_sd1Id, jni_sd1);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_ssbmv (JNIEnv * env, jobject calling_obj, jstring uplo, jint n, jint k, jfloat alpha, jfloatArray a, jint lda, jfloatArray x, jint incx, jfloat beta, jfloatArray y, jint incy) {
  char * jni_uplo = (char *)(*env)->GetStringUTFChars(env, uplo, JNI_FALSE);
  jfloat * jni_a = NULL;
  if (a != NULL) {
    jni_a = (*env)->GetPrimitiveArrayCritical(env, a, JNI_FALSE);
    check_memory(env, jni_a);
  }
  jfloat * jni_x = NULL;
  if (x != NULL) {
    jni_x = (*env)->GetPrimitiveArrayCritical(env, x, JNI_FALSE);
    check_memory(env, jni_x);
  }
  jfloat * jni_y = NULL;
  if (y != NULL) {
    jni_y = (*env)->GetPrimitiveArrayCritical(env, y, JNI_FALSE);
    check_memory(env, jni_y);
  }
cblas_ssbmv(CblasColMajor, getCblasUpLo(jni_uplo), n, k, alpha, jni_a, lda, jni_x, incx, beta, jni_y, incy);
  (*env)->ReleaseStringUTFChars(env, uplo, jni_uplo);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_ssbmv_1offsets (JNIEnv * env, jobject calling_obj, jstring uplo, jint n, jint k, jfloat alpha, jfloatArray a, jint _a_offset, jint lda, jfloatArray x, jint _x_offset, jint incx, jfloat beta, jfloatArray y, jint _y_offset, jint incy) {
  char * jni_uplo = (char *)(*env)->GetStringUTFChars(env, uplo, JNI_FALSE);
  jfloat * jni_a = NULL;
  if (a != NULL) {
    jni_a = (*env)->GetPrimitiveArrayCritical(env, a, JNI_FALSE);
    check_memory(env, jni_a);
  }
  jfloat * jni_x = NULL;
  if (x != NULL) {
    jni_x = (*env)->GetPrimitiveArrayCritical(env, x, JNI_FALSE);
    check_memory(env, jni_x);
  }
  jfloat * jni_y = NULL;
  if (y != NULL) {
    jni_y = (*env)->GetPrimitiveArrayCritical(env, y, JNI_FALSE);
    check_memory(env, jni_y);
  }
cblas_ssbmv(CblasColMajor, getCblasUpLo(jni_uplo), n, k, alpha, jni_a + _a_offset, lda, jni_x + _x_offset, incx, beta, jni_y + _y_offset, incy);
  (*env)->ReleaseStringUTFChars(env, uplo, jni_uplo);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_sscal (JNIEnv * env, jobject calling_obj, jint n, jfloat sa, jfloatArray sx, jint incx) {
  jfloat * jni_sx = NULL;
  if (sx != NULL) {
    jni_sx = (*env)->GetPrimitiveArrayCritical(env, sx, JNI_FALSE);
    check_memory(env, jni_sx);
  }
cblas_sscal(n, sa, jni_sx, incx);
  if (sx != NULL) {
    (*env)->ReleasePrimitiveArrayCritical(env, sx, jni_sx, 0);
  }
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_sscal_1offsets (JNIEnv * env, jobject calling_obj, jint n, jfloat sa, jfloatArray sx, jint _sx_offset, jint incx) {
  jfloat * jni_sx = NULL;
  if (sx != NULL) {
    jni_sx = (*env)->GetPrimitiveArrayCritical(env, sx, JNI_FALSE);
    check_memory(env, jni_sx);
  }
cblas_sscal(n, sa, jni_sx + _sx_offset, incx);
  if (sx != NULL) {
    (*env)->ReleasePrimitiveArrayCritical(env, sx, jni_sx, 0);
  }
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_sspmv (JNIEnv * env, jobject calling_obj, jstring uplo, jint n, jfloat alpha, jfloatArray ap, jfloatArray x, jint incx, jfloat beta, jfloatArray y, jint incy) {
  char * jni_uplo = (char *)(*env)->GetStringUTFChars(env, uplo, JNI_FALSE);
  jfloat * jni_ap = NULL;
  if (ap != NULL) {
    jni_ap = (*env)->GetPrimitiveArrayCritical(env, ap, JNI_FALSE);
    check_memory(env, jni_ap);
  }
  jfloat * jni_x = NULL;
  if (x != NULL) {
    jni_x = (*env)->GetPrimitiveArrayCritical(env, x, JNI_FALSE);
    check_memory(env, jni_x);
  }
  jfloat * jni_y = NULL;
  if (y != NULL) {
    jni_y = (*env)->GetPrimitiveArrayCritical(env, y, JNI_FALSE);
    check_memory(env, jni_y);
  }
cblas_sspmv(CblasColMajor, getCblasUpLo(jni_uplo), n, alpha, jni_ap, jni_x, incx, beta, jni_y, incy);
  (*env)->ReleaseStringUTFChars(env, uplo, jni_uplo);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_sspmv_1offsets (JNIEnv * env, jobject calling_obj, jstring uplo, jint n, jfloat alpha, jfloatArray ap, jint _ap_offset, jfloatArray x, jint _x_offset, jint incx, jfloat beta, jfloatArray y, jint _y_offset, jint incy) {
  char * jni_uplo = (char *)(*env)->GetStringUTFChars(env, uplo, JNI_FALSE);
  jfloat * jni_ap = NULL;
  if (ap != NULL) {
    jni_ap = (*env)->GetPrimitiveArrayCritical(env, ap, JNI_FALSE);
    check_memory(env, jni_ap);
  }
  jfloat * jni_x = NULL;
  if (x != NULL) {
    jni_x = (*env)->GetPrimitiveArrayCritical(env, x, JNI_FALSE);
    check_memory(env, jni_x);
  }
  jfloat * jni_y = NULL;
  if (y != NULL) {
    jni_y = (*env)->GetPrimitiveArrayCritical(env, y, JNI_FALSE);
    check_memory(env, jni_y);
  }
cblas_sspmv(CblasColMajor, getCblasUpLo(jni_uplo), n, alpha, jni_ap + _ap_offset, jni_x + _x_offset, incx, beta, jni_y + _y_offset, incy);
  (*env)->ReleaseStringUTFChars(env, uplo, jni_uplo);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_sspr (JNIEnv * env, jobject calling_obj, jstring uplo, jint n, jfloat alpha, jfloatArray x, jint incx, jfloatArray ap) {
  char * jni_uplo = (char *)(*env)->GetStringUTFChars(env, uplo, JNI_FALSE);
  jfloat * jni_x = NULL;
  if (x != NULL) {
    jni_x = (*env)->GetPrimitiveArrayCritical(env, x, JNI_FALSE);
    check_memory(env, jni_x);
  }
  jfloat * jni_ap = NULL;
  if (ap != NULL) {
    jni_ap = (*env)->GetPrimitiveArrayCritical(env, ap, JNI_FALSE);
    check_memory(env, jni_ap);
  }
cblas_sspr(CblasColMajor, getCblasUpLo(jni_uplo), n, alpha, jni_x, incx, jni_ap);
  (*env)->ReleaseStringUTFChars(env, uplo, jni_uplo);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_sspr_1offsets (JNIEnv * env, jobject calling_obj, jstring uplo, jint n, jfloat alpha, jfloatArray x, jint _x_offset, jint incx, jfloatArray ap, jint _ap_offset) {
  char * jni_uplo = (char *)(*env)->GetStringUTFChars(env, uplo, JNI_FALSE);
  jfloat * jni_x = NULL;
  if (x != NULL) {
    jni_x = (*env)->GetPrimitiveArrayCritical(env, x, JNI_FALSE);
    check_memory(env, jni_x);
  }
  jfloat * jni_ap = NULL;
  if (ap != NULL) {
    jni_ap = (*env)->GetPrimitiveArrayCritical(env, ap, JNI_FALSE);
    check_memory(env, jni_ap);
  }
cblas_sspr(CblasColMajor, getCblasUpLo(jni_uplo), n, alpha, jni_x + _x_offset, incx, jni_ap + _ap_offset);
  (*env)->ReleaseStringUTFChars(env, uplo, jni_uplo);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_sspr2 (JNIEnv * env, jobject calling_obj, jstring uplo, jint n, jfloat alpha, jfloatArray x, jint incx, jfloatArray y, jint incy, jfloatArray ap) {
  char * jni_uplo = (char *)(*env)->GetStringUTFChars(env, uplo, JNI_FALSE);
  jfloat * jni_x = NULL;
  if (x != NULL) {
    jni_x = (*env)->GetPrimitiveArrayCritical(env, x, JNI_FALSE);
    check_memory(env, jni_x);
  }
  jfloat * jni_y = NULL;
  if (y != NULL) {
    jni_y = (*env)->GetPrimitiveArrayCritical(env, y, JNI_FALSE);
    check_memory(env, jni_y);
  }
  jfloat * jni_ap = NULL;
  if (ap != NULL) {
    jni_ap = (*env)->GetPrimitiveArrayCritical(env, ap, JNI_FALSE);
    check_memory(env, jni_ap);
  }
cblas_sspr2(CblasColMajor, getCblasUpLo(jni_uplo), n, alpha, jni_x, incx, jni_y, incy, jni_ap);
  (*env)->ReleaseStringUTFChars(env, uplo, jni_uplo);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_sspr2_1offsets (JNIEnv * env, jobject calling_obj, jstring uplo, jint n, jfloat alpha, jfloatArray x, jint _x_offset, jint incx, jfloatArray y, jint _y_offset, jint incy, jfloatArray ap, jint _ap_offset) {
  char * jni_uplo = (char *)(*env)->GetStringUTFChars(env, uplo, JNI_FALSE);
  jfloat * jni_x = NULL;
  if (x != NULL) {
    jni_x = (*env)->GetPrimitiveArrayCritical(env, x, JNI_FALSE);
    check_memory(env, jni_x);
  }
  jfloat * jni_y = NULL;
  if (y != NULL) {
    jni_y = (*env)->GetPrimitiveArrayCritical(env, y, JNI_FALSE);
    check_memory(env, jni_y);
  }
  jfloat * jni_ap = NULL;
  if (ap != NULL) {
    jni_ap = (*env)->GetPrimitiveArrayCritical(env, ap, JNI_FALSE);
    check_memory(env, jni_ap);
  }
cblas_sspr2(CblasColMajor, getCblasUpLo(jni_uplo), n, alpha, jni_x + _x_offset, incx, jni_y + _y_offset, incy, jni_ap + _ap_offset);
  (*env)->ReleaseStringUTFChars(env, uplo, jni_uplo);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_sswap (JNIEnv * env, jobject calling_obj, jint n, jfloatArray sx, jint incx, jfloatArray sy, jint incy) {
  jfloat * jni_sx = NULL;
  if (sx != NULL) {
    jni_sx = (*env)->GetPrimitiveArrayCritical(env, sx, JNI_FALSE);
    check_memory(env, jni_sx);
  }
  jfloat * jni_sy = NULL;
  if (sy != NULL) {
    jni_sy = (*env)->GetPrimitiveArrayCritical(env, sy, JNI_FALSE);
    check_memory(env, jni_sy);
  }
cblas_sswap(n, jni_sx, incx, jni_sy, incy);
  if (sy != NULL) {
    (*env)->ReleasePrimitiveArrayCritical(env, sy, jni_sy, 0);
  }
  if (sx != NULL) {
    (*env)->ReleasePrimitiveArrayCritical(env, sx, jni_sx, 0);
  }
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_sswap_1offsets (JNIEnv * env, jobject calling_obj, jint n, jfloatArray sx, jint _sx_offset, jint incx, jfloatArray sy, jint _sy_offset, jint incy) {
  jfloat * jni_sx = NULL;
  if (sx != NULL) {
    jni_sx = (*env)->GetPrimitiveArrayCritical(env, sx, JNI_FALSE);
    check_memory(env, jni_sx);
  }
  jfloat * jni_sy = NULL;
  if (sy != NULL) {
    jni_sy = (*env)->GetPrimitiveArrayCritical(env, sy, JNI_FALSE);
    check_memory(env, jni_sy);
  }
cblas_sswap(n, jni_sx + _sx_offset, incx, jni_sy + _sy_offset, incy);
  if (sy != NULL) {
    (*env)->ReleasePrimitiveArrayCritical(env, sy, jni_sy, 0);
  }
  if (sx != NULL) {
    (*env)->ReleasePrimitiveArrayCritical(env, sx, jni_sx, 0);
  }
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_ssymm (JNIEnv * env, jobject calling_obj, jstring side, jstring uplo, jint m, jint n, jfloat alpha, jfloatArray a, jint lda, jfloatArray b, jint ldb, jfloat beta, jfloatArray c, jint Ldc) {
  char * jni_side = (char *)(*env)->GetStringUTFChars(env, side, JNI_FALSE);
  char * jni_uplo = (char *)(*env)->GetStringUTFChars(env, uplo, JNI_FALSE);
  jfloat * jni_a = NULL;
  if (a != NULL) {
    jni_a = (*env)->GetPrimitiveArrayCritical(env, a, JNI_FALSE);
    check_memory(env, jni_a);
  }
  jfloat * jni_b = NULL;
  if (b != NULL) {
    jni_b = (*env)->GetPrimitiveArrayCritical(env, b, JNI_FALSE);
    check_memory(env, jni_b);
  }
  jfloat * jni_c = NULL;
  if (c != NULL) {
    jni_c = (*env)->GetPrimitiveArrayCritical(env, c, JNI_FALSE);
    check_memory(env, jni_c);
  }
cblas_ssymm(CblasColMajor, getCblasSide(jni_side), getCblasUpLo(jni_uplo), m, n, alpha, jni_a, lda, jni_b, ldb, beta, jni_c, Ldc);
  (*env)->ReleaseStringUTFChars(env, uplo, jni_uplo);
  (*env)->ReleaseStringUTFChars(env, side, jni_side);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_ssymm_1offsets (JNIEnv * env, jobject calling_obj, jstring side, jstring uplo, jint m, jint n, jfloat alpha, jfloatArray a, jint _a_offset, jint lda, jfloatArray b, jint _b_offset, jint ldb, jfloat beta, jfloatArray c, jint _c_offset, jint Ldc) {
  char * jni_side = (char *)(*env)->GetStringUTFChars(env, side, JNI_FALSE);
  char * jni_uplo = (char *)(*env)->GetStringUTFChars(env, uplo, JNI_FALSE);
  jfloat * jni_a = NULL;
  if (a != NULL) {
    jni_a = (*env)->GetPrimitiveArrayCritical(env, a, JNI_FALSE);
    check_memory(env, jni_a);
  }
  jfloat * jni_b = NULL;
  if (b != NULL) {
    jni_b = (*env)->GetPrimitiveArrayCritical(env, b, JNI_FALSE);
    check_memory(env, jni_b);
  }
  jfloat * jni_c = NULL;
  if (c != NULL) {
    jni_c = (*env)->GetPrimitiveArrayCritical(env, c, JNI_FALSE);
    check_memory(env, jni_c);
  }
cblas_ssymm(CblasColMajor, getCblasSide(jni_side), getCblasUpLo(jni_uplo), m, n, alpha, jni_a + _a_offset, lda, jni_b + _b_offset, ldb, beta, jni_c + _c_offset, Ldc);
  (*env)->ReleaseStringUTFChars(env, uplo, jni_uplo);
  (*env)->ReleaseStringUTFChars(env, side, jni_side);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_ssymv (JNIEnv * env, jobject calling_obj, jstring uplo, jint n, jfloat alpha, jfloatArray a, jint lda, jfloatArray x, jint incx, jfloat beta, jfloatArray y, jint incy) {
  char * jni_uplo = (char *)(*env)->GetStringUTFChars(env, uplo, JNI_FALSE);
  jfloat * jni_a = NULL;
  if (a != NULL) {
    jni_a = (*env)->GetPrimitiveArrayCritical(env, a, JNI_FALSE);
    check_memory(env, jni_a);
  }
  jfloat * jni_x = NULL;
  if (x != NULL) {
    jni_x = (*env)->GetPrimitiveArrayCritical(env, x, JNI_FALSE);
    check_memory(env, jni_x);
  }
  jfloat * jni_y = NULL;
  if (y != NULL) {
    jni_y = (*env)->GetPrimitiveArrayCritical(env, y, JNI_FALSE);
    check_memory(env, jni_y);
  }
cblas_ssymv(CblasColMajor, getCblasUpLo(jni_uplo), n, alpha, jni_a, lda, jni_x, incx, beta, jni_y, incy);
  (*env)->ReleaseStringUTFChars(env, uplo, jni_uplo);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_ssymv_1offsets (JNIEnv * env, jobject calling_obj, jstring uplo, jint n, jfloat alpha, jfloatArray a, jint _a_offset, jint lda, jfloatArray x, jint _x_offset, jint incx, jfloat beta, jfloatArray y, jint _y_offset, jint incy) {
  char * jni_uplo = (char *)(*env)->GetStringUTFChars(env, uplo, JNI_FALSE);
  jfloat * jni_a = NULL;
  if (a != NULL) {
    jni_a = (*env)->GetPrimitiveArrayCritical(env, a, JNI_FALSE);
    check_memory(env, jni_a);
  }
  jfloat * jni_x = NULL;
  if (x != NULL) {
    jni_x = (*env)->GetPrimitiveArrayCritical(env, x, JNI_FALSE);
    check_memory(env, jni_x);
  }
  jfloat * jni_y = NULL;
  if (y != NULL) {
    jni_y = (*env)->GetPrimitiveArrayCritical(env, y, JNI_FALSE);
    check_memory(env, jni_y);
  }
cblas_ssymv(CblasColMajor, getCblasUpLo(jni_uplo), n, alpha, jni_a + _a_offset, lda, jni_x + _x_offset, incx, beta, jni_y + _y_offset, incy);
  (*env)->ReleaseStringUTFChars(env, uplo, jni_uplo);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_ssyr (JNIEnv * env, jobject calling_obj, jstring uplo, jint n, jfloat alpha, jfloatArray x, jint incx, jfloatArray a, jint lda) {
  char * jni_uplo = (char *)(*env)->GetStringUTFChars(env, uplo, JNI_FALSE);
  jfloat * jni_x = NULL;
  if (x != NULL) {
    jni_x = (*env)->GetPrimitiveArrayCritical(env, x, JNI_FALSE);
    check_memory(env, jni_x);
  }
  jfloat * jni_a = NULL;
  if (a != NULL) {
    jni_a = (*env)->GetPrimitiveArrayCritical(env, a, JNI_FALSE);
    check_memory(env, jni_a);
  }
cblas_ssyr(CblasColMajor, getCblasUpLo(jni_uplo), n, alpha, jni_x, incx, jni_a, lda);
  (*env)->ReleaseStringUTFChars(env, uplo, jni_uplo);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_ssyr_1offsets (JNIEnv * env, jobject calling_obj, jstring uplo, jint n, jfloat alpha, jfloatArray x, jint _x_offset, jint incx, jfloatArray a, jint _a_offset, jint lda) {
  char * jni_uplo = (char *)(*env)->GetStringUTFChars(env, uplo, JNI_FALSE);
  jfloat * jni_x = NULL;
  if (x != NULL) {
    jni_x = (*env)->GetPrimitiveArrayCritical(env, x, JNI_FALSE);
    check_memory(env, jni_x);
  }
  jfloat * jni_a = NULL;
  if (a != NULL) {
    jni_a = (*env)->GetPrimitiveArrayCritical(env, a, JNI_FALSE);
    check_memory(env, jni_a);
  }
cblas_ssyr(CblasColMajor, getCblasUpLo(jni_uplo), n, alpha, jni_x + _x_offset, incx, jni_a + _a_offset, lda);
  (*env)->ReleaseStringUTFChars(env, uplo, jni_uplo);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_ssyr2 (JNIEnv * env, jobject calling_obj, jstring uplo, jint n, jfloat alpha, jfloatArray x, jint incx, jfloatArray y, jint incy, jfloatArray a, jint lda) {
  char * jni_uplo = (char *)(*env)->GetStringUTFChars(env, uplo, JNI_FALSE);
  jfloat * jni_x = NULL;
  if (x != NULL) {
    jni_x = (*env)->GetPrimitiveArrayCritical(env, x, JNI_FALSE);
    check_memory(env, jni_x);
  }
  jfloat * jni_y = NULL;
  if (y != NULL) {
    jni_y = (*env)->GetPrimitiveArrayCritical(env, y, JNI_FALSE);
    check_memory(env, jni_y);
  }
  jfloat * jni_a = NULL;
  if (a != NULL) {
    jni_a = (*env)->GetPrimitiveArrayCritical(env, a, JNI_FALSE);
    check_memory(env, jni_a);
  }
cblas_ssyr2(CblasColMajor, getCblasUpLo(jni_uplo), n, alpha, jni_x, incx, jni_y, incy, jni_a, lda);
  (*env)->ReleaseStringUTFChars(env, uplo, jni_uplo);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_ssyr2_1offsets (JNIEnv * env, jobject calling_obj, jstring uplo, jint n, jfloat alpha, jfloatArray x, jint _x_offset, jint incx, jfloatArray y, jint _y_offset, jint incy, jfloatArray a, jint _a_offset, jint lda) {
  char * jni_uplo = (char *)(*env)->GetStringUTFChars(env, uplo, JNI_FALSE);
  jfloat * jni_x = NULL;
  if (x != NULL) {
    jni_x = (*env)->GetPrimitiveArrayCritical(env, x, JNI_FALSE);
    check_memory(env, jni_x);
  }
  jfloat * jni_y = NULL;
  if (y != NULL) {
    jni_y = (*env)->GetPrimitiveArrayCritical(env, y, JNI_FALSE);
    check_memory(env, jni_y);
  }
  jfloat * jni_a = NULL;
  if (a != NULL) {
    jni_a = (*env)->GetPrimitiveArrayCritical(env, a, JNI_FALSE);
    check_memory(env, jni_a);
  }
cblas_ssyr2(CblasColMajor, getCblasUpLo(jni_uplo), n, alpha, jni_x + _x_offset, incx, jni_y + _y_offset, incy, jni_a + _a_offset, lda);
  (*env)->ReleaseStringUTFChars(env, uplo, jni_uplo);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_ssyr2k (JNIEnv * env, jobject calling_obj, jstring uplo, jstring trans, jint n, jint k, jfloat alpha, jfloatArray a, jint lda, jfloatArray b, jint ldb, jfloat beta, jfloatArray c, jint Ldc) {
  char * jni_uplo = (char *)(*env)->GetStringUTFChars(env, uplo, JNI_FALSE);
  char * jni_trans = (char *)(*env)->GetStringUTFChars(env, trans, JNI_FALSE);
  jfloat * jni_a = NULL;
  if (a != NULL) {
    jni_a = (*env)->GetPrimitiveArrayCritical(env, a, JNI_FALSE);
    check_memory(env, jni_a);
  }
  jfloat * jni_b = NULL;
  if (b != NULL) {
    jni_b = (*env)->GetPrimitiveArrayCritical(env, b, JNI_FALSE);
    check_memory(env, jni_b);
  }
  jfloat * jni_c = NULL;
  if (c != NULL) {
    jni_c = (*env)->GetPrimitiveArrayCritical(env, c, JNI_FALSE);
    check_memory(env, jni_c);
  }
cblas_ssyr2k(CblasColMajor, getCblasUpLo(jni_uplo), getCblasTrans(jni_trans), n, k, alpha, jni_a, lda, jni_b, ldb, beta, jni_c, Ldc);
  (*env)->ReleaseStringUTFChars(env, trans, jni_trans);
  (*env)->ReleaseStringUTFChars(env, uplo, jni_uplo);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_ssyr2k_1offsets (JNIEnv * env, jobject calling_obj, jstring uplo, jstring trans, jint n, jint k, jfloat alpha, jfloatArray a, jint _a_offset, jint lda, jfloatArray b, jint _b_offset, jint ldb, jfloat beta, jfloatArray c, jint _c_offset, jint Ldc) {
  char * jni_uplo = (char *)(*env)->GetStringUTFChars(env, uplo, JNI_FALSE);
  char * jni_trans = (char *)(*env)->GetStringUTFChars(env, trans, JNI_FALSE);
  jfloat * jni_a = NULL;
  if (a != NULL) {
    jni_a = (*env)->GetPrimitiveArrayCritical(env, a, JNI_FALSE);
    check_memory(env, jni_a);
  }
  jfloat * jni_b = NULL;
  if (b != NULL) {
    jni_b = (*env)->GetPrimitiveArrayCritical(env, b, JNI_FALSE);
    check_memory(env, jni_b);
  }
  jfloat * jni_c = NULL;
  if (c != NULL) {
    jni_c = (*env)->GetPrimitiveArrayCritical(env, c, JNI_FALSE);
    check_memory(env, jni_c);
  }
cblas_ssyr2k(CblasColMajor, getCblasUpLo(jni_uplo), getCblasTrans(jni_trans), n, k, alpha, jni_a + _a_offset, lda, jni_b + _b_offset, ldb, beta, jni_c + _c_offset, Ldc);
  (*env)->ReleaseStringUTFChars(env, trans, jni_trans);
  (*env)->ReleaseStringUTFChars(env, uplo, jni_uplo);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_ssyrk (JNIEnv * env, jobject calling_obj, jstring uplo, jstring trans, jint n, jint k, jfloat alpha, jfloatArray a, jint lda, jfloat beta, jfloatArray c, jint Ldc) {
  char * jni_uplo = (char *)(*env)->GetStringUTFChars(env, uplo, JNI_FALSE);
  char * jni_trans = (char *)(*env)->GetStringUTFChars(env, trans, JNI_FALSE);
  jfloat * jni_a = NULL;
  if (a != NULL) {
    jni_a = (*env)->GetPrimitiveArrayCritical(env, a, JNI_FALSE);
    check_memory(env, jni_a);
  }
  jfloat * jni_c = NULL;
  if (c != NULL) {
    jni_c = (*env)->GetPrimitiveArrayCritical(env, c, JNI_FALSE);
    check_memory(env, jni_c);
  }
cblas_ssyrk(CblasColMajor, getCblasUpLo(jni_uplo), getCblasTrans(jni_trans), n, k, alpha, jni_a, lda, beta, jni_c, Ldc);
  (*env)->ReleaseStringUTFChars(env, trans, jni_trans);
  (*env)->ReleaseStringUTFChars(env, uplo, jni_uplo);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_ssyrk_1offsets (JNIEnv * env, jobject calling_obj, jstring uplo, jstring trans, jint n, jint k, jfloat alpha, jfloatArray a, jint _a_offset, jint lda, jfloat beta, jfloatArray c, jint _c_offset, jint Ldc) {
  char * jni_uplo = (char *)(*env)->GetStringUTFChars(env, uplo, JNI_FALSE);
  char * jni_trans = (char *)(*env)->GetStringUTFChars(env, trans, JNI_FALSE);
  jfloat * jni_a = NULL;
  if (a != NULL) {
    jni_a = (*env)->GetPrimitiveArrayCritical(env, a, JNI_FALSE);
    check_memory(env, jni_a);
  }
  jfloat * jni_c = NULL;
  if (c != NULL) {
    jni_c = (*env)->GetPrimitiveArrayCritical(env, c, JNI_FALSE);
    check_memory(env, jni_c);
  }
cblas_ssyrk(CblasColMajor, getCblasUpLo(jni_uplo), getCblasTrans(jni_trans), n, k, alpha, jni_a + _a_offset, lda, beta, jni_c + _c_offset, Ldc);
  (*env)->ReleaseStringUTFChars(env, trans, jni_trans);
  (*env)->ReleaseStringUTFChars(env, uplo, jni_uplo);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_stbmv (JNIEnv * env, jobject calling_obj, jstring uplo, jstring trans, jstring diag, jint n, jint k, jfloatArray a, jint lda, jfloatArray x, jint incx) {
  char * jni_uplo = (char *)(*env)->GetStringUTFChars(env, uplo, JNI_FALSE);
  char * jni_trans = (char *)(*env)->GetStringUTFChars(env, trans, JNI_FALSE);
  char * jni_diag = (char *)(*env)->GetStringUTFChars(env, diag, JNI_FALSE);
  jfloat * jni_a = NULL;
  if (a != NULL) {
    jni_a = (*env)->GetPrimitiveArrayCritical(env, a, JNI_FALSE);
    check_memory(env, jni_a);
  }
  jfloat * jni_x = NULL;
  if (x != NULL) {
    jni_x = (*env)->GetPrimitiveArrayCritical(env, x, JNI_FALSE);
    check_memory(env, jni_x);
  }
cblas_stbmv(CblasColMajor, getCblasUpLo(jni_uplo), getCblasTrans(jni_trans), getCblasDiag(jni_diag), n, k, jni_a, lda, jni_x, incx);
  (*env)->ReleaseStringUTFChars(env, diag, jni_diag);
  (*env)->ReleaseStringUTFChars(env, trans, jni_trans);
  (*env)->ReleaseStringUTFChars(env, uplo, jni_uplo);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_stbmv_1offsets (JNIEnv * env, jobject calling_obj, jstring uplo, jstring trans, jstring diag, jint n, jint k, jfloatArray a, jint _a_offset, jint lda, jfloatArray x, jint _x_offset, jint incx) {
  char * jni_uplo = (char *)(*env)->GetStringUTFChars(env, uplo, JNI_FALSE);
  char * jni_trans = (char *)(*env)->GetStringUTFChars(env, trans, JNI_FALSE);
  char * jni_diag = (char *)(*env)->GetStringUTFChars(env, diag, JNI_FALSE);
  jfloat * jni_a = NULL;
  if (a != NULL) {
    jni_a = (*env)->GetPrimitiveArrayCritical(env, a, JNI_FALSE);
    check_memory(env, jni_a);
  }
  jfloat * jni_x = NULL;
  if (x != NULL) {
    jni_x = (*env)->GetPrimitiveArrayCritical(env, x, JNI_FALSE);
    check_memory(env, jni_x);
  }
cblas_stbmv(CblasColMajor, getCblasUpLo(jni_uplo), getCblasTrans(jni_trans), getCblasDiag(jni_diag), n, k, jni_a + _a_offset, lda, jni_x + _x_offset, incx);
  (*env)->ReleaseStringUTFChars(env, diag, jni_diag);
  (*env)->ReleaseStringUTFChars(env, trans, jni_trans);
  (*env)->ReleaseStringUTFChars(env, uplo, jni_uplo);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_stbsv (JNIEnv * env, jobject calling_obj, jstring uplo, jstring trans, jstring diag, jint n, jint k, jfloatArray a, jint lda, jfloatArray x, jint incx) {
  char * jni_uplo = (char *)(*env)->GetStringUTFChars(env, uplo, JNI_FALSE);
  char * jni_trans = (char *)(*env)->GetStringUTFChars(env, trans, JNI_FALSE);
  char * jni_diag = (char *)(*env)->GetStringUTFChars(env, diag, JNI_FALSE);
  jfloat * jni_a = NULL;
  if (a != NULL) {
    jni_a = (*env)->GetPrimitiveArrayCritical(env, a, JNI_FALSE);
    check_memory(env, jni_a);
  }
  jfloat * jni_x = NULL;
  if (x != NULL) {
    jni_x = (*env)->GetPrimitiveArrayCritical(env, x, JNI_FALSE);
    check_memory(env, jni_x);
  }
cblas_stbsv(CblasColMajor, getCblasUpLo(jni_uplo), getCblasTrans(jni_trans), getCblasDiag(jni_diag), n, k, jni_a, lda, jni_x, incx);
  (*env)->ReleaseStringUTFChars(env, diag, jni_diag);
  (*env)->ReleaseStringUTFChars(env, trans, jni_trans);
  (*env)->ReleaseStringUTFChars(env, uplo, jni_uplo);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_stbsv_1offsets (JNIEnv * env, jobject calling_obj, jstring uplo, jstring trans, jstring diag, jint n, jint k, jfloatArray a, jint _a_offset, jint lda, jfloatArray x, jint _x_offset, jint incx) {
  char * jni_uplo = (char *)(*env)->GetStringUTFChars(env, uplo, JNI_FALSE);
  char * jni_trans = (char *)(*env)->GetStringUTFChars(env, trans, JNI_FALSE);
  char * jni_diag = (char *)(*env)->GetStringUTFChars(env, diag, JNI_FALSE);
  jfloat * jni_a = NULL;
  if (a != NULL) {
    jni_a = (*env)->GetPrimitiveArrayCritical(env, a, JNI_FALSE);
    check_memory(env, jni_a);
  }
  jfloat * jni_x = NULL;
  if (x != NULL) {
    jni_x = (*env)->GetPrimitiveArrayCritical(env, x, JNI_FALSE);
    check_memory(env, jni_x);
  }
cblas_stbsv(CblasColMajor, getCblasUpLo(jni_uplo), getCblasTrans(jni_trans), getCblasDiag(jni_diag), n, k, jni_a + _a_offset, lda, jni_x + _x_offset, incx);
  (*env)->ReleaseStringUTFChars(env, diag, jni_diag);
  (*env)->ReleaseStringUTFChars(env, trans, jni_trans);
  (*env)->ReleaseStringUTFChars(env, uplo, jni_uplo);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_stpmv (JNIEnv * env, jobject calling_obj, jstring uplo, jstring trans, jstring diag, jint n, jfloatArray ap, jfloatArray x, jint incx) {
  char * jni_uplo = (char *)(*env)->GetStringUTFChars(env, uplo, JNI_FALSE);
  char * jni_trans = (char *)(*env)->GetStringUTFChars(env, trans, JNI_FALSE);
  char * jni_diag = (char *)(*env)->GetStringUTFChars(env, diag, JNI_FALSE);
  jfloat * jni_ap = NULL;
  if (ap != NULL) {
    jni_ap = (*env)->GetPrimitiveArrayCritical(env, ap, JNI_FALSE);
    check_memory(env, jni_ap);
  }
  jfloat * jni_x = NULL;
  if (x != NULL) {
    jni_x = (*env)->GetPrimitiveArrayCritical(env, x, JNI_FALSE);
    check_memory(env, jni_x);
  }
cblas_stpmv(CblasColMajor, getCblasUpLo(jni_uplo), getCblasTrans(jni_trans), getCblasDiag(jni_diag), n, jni_ap, jni_x, incx);
  (*env)->ReleaseStringUTFChars(env, diag, jni_diag);
  (*env)->ReleaseStringUTFChars(env, trans, jni_trans);
  (*env)->ReleaseStringUTFChars(env, uplo, jni_uplo);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_stpmv_1offsets (JNIEnv * env, jobject calling_obj, jstring uplo, jstring trans, jstring diag, jint n, jfloatArray ap, jint _ap_offset, jfloatArray x, jint _x_offset, jint incx) {
  char * jni_uplo = (char *)(*env)->GetStringUTFChars(env, uplo, JNI_FALSE);
  char * jni_trans = (char *)(*env)->GetStringUTFChars(env, trans, JNI_FALSE);
  char * jni_diag = (char *)(*env)->GetStringUTFChars(env, diag, JNI_FALSE);
  jfloat * jni_ap = NULL;
  if (ap != NULL) {
    jni_ap = (*env)->GetPrimitiveArrayCritical(env, ap, JNI_FALSE);
    check_memory(env, jni_ap);
  }
  jfloat * jni_x = NULL;
  if (x != NULL) {
    jni_x = (*env)->GetPrimitiveArrayCritical(env, x, JNI_FALSE);
    check_memory(env, jni_x);
  }
cblas_stpmv(CblasColMajor, getCblasUpLo(jni_uplo), getCblasTrans(jni_trans), getCblasDiag(jni_diag), n, jni_ap + _ap_offset, jni_x + _x_offset, incx);
  (*env)->ReleaseStringUTFChars(env, diag, jni_diag);
  (*env)->ReleaseStringUTFChars(env, trans, jni_trans);
  (*env)->ReleaseStringUTFChars(env, uplo, jni_uplo);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_stpsv (JNIEnv * env, jobject calling_obj, jstring uplo, jstring trans, jstring diag, jint n, jfloatArray ap, jfloatArray x, jint incx) {
  char * jni_uplo = (char *)(*env)->GetStringUTFChars(env, uplo, JNI_FALSE);
  char * jni_trans = (char *)(*env)->GetStringUTFChars(env, trans, JNI_FALSE);
  char * jni_diag = (char *)(*env)->GetStringUTFChars(env, diag, JNI_FALSE);
  jfloat * jni_ap = NULL;
  if (ap != NULL) {
    jni_ap = (*env)->GetPrimitiveArrayCritical(env, ap, JNI_FALSE);
    check_memory(env, jni_ap);
  }
  jfloat * jni_x = NULL;
  if (x != NULL) {
    jni_x = (*env)->GetPrimitiveArrayCritical(env, x, JNI_FALSE);
    check_memory(env, jni_x);
  }
cblas_stpsv(CblasColMajor, getCblasUpLo(jni_uplo), getCblasTrans(jni_trans), getCblasDiag(jni_diag), n, jni_ap, jni_x, incx);
  (*env)->ReleaseStringUTFChars(env, diag, jni_diag);
  (*env)->ReleaseStringUTFChars(env, trans, jni_trans);
  (*env)->ReleaseStringUTFChars(env, uplo, jni_uplo);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_stpsv_1offsets (JNIEnv * env, jobject calling_obj, jstring uplo, jstring trans, jstring diag, jint n, jfloatArray ap, jint _ap_offset, jfloatArray x, jint _x_offset, jint incx) {
  char * jni_uplo = (char *)(*env)->GetStringUTFChars(env, uplo, JNI_FALSE);
  char * jni_trans = (char *)(*env)->GetStringUTFChars(env, trans, JNI_FALSE);
  char * jni_diag = (char *)(*env)->GetStringUTFChars(env, diag, JNI_FALSE);
  jfloat * jni_ap = NULL;
  if (ap != NULL) {
    jni_ap = (*env)->GetPrimitiveArrayCritical(env, ap, JNI_FALSE);
    check_memory(env, jni_ap);
  }
  jfloat * jni_x = NULL;
  if (x != NULL) {
    jni_x = (*env)->GetPrimitiveArrayCritical(env, x, JNI_FALSE);
    check_memory(env, jni_x);
  }
cblas_stpsv(CblasColMajor, getCblasUpLo(jni_uplo), getCblasTrans(jni_trans), getCblasDiag(jni_diag), n, jni_ap + _ap_offset, jni_x + _x_offset, incx);
  (*env)->ReleaseStringUTFChars(env, diag, jni_diag);
  (*env)->ReleaseStringUTFChars(env, trans, jni_trans);
  (*env)->ReleaseStringUTFChars(env, uplo, jni_uplo);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_strmm (JNIEnv * env, jobject calling_obj, jstring side, jstring uplo, jstring transa, jstring diag, jint m, jint n, jfloat alpha, jfloatArray a, jint lda, jfloatArray b, jint ldb) {
  char * jni_side = (char *)(*env)->GetStringUTFChars(env, side, JNI_FALSE);
  char * jni_uplo = (char *)(*env)->GetStringUTFChars(env, uplo, JNI_FALSE);
  char * jni_transa = (char *)(*env)->GetStringUTFChars(env, transa, JNI_FALSE);
  char * jni_diag = (char *)(*env)->GetStringUTFChars(env, diag, JNI_FALSE);
  jfloat * jni_a = NULL;
  if (a != NULL) {
    jni_a = (*env)->GetPrimitiveArrayCritical(env, a, JNI_FALSE);
    check_memory(env, jni_a);
  }
  jfloat * jni_b = NULL;
  if (b != NULL) {
    jni_b = (*env)->GetPrimitiveArrayCritical(env, b, JNI_FALSE);
    check_memory(env, jni_b);
  }
cblas_strmm(CblasColMajor, getCblasSide(jni_side), getCblasUpLo(jni_uplo), getCblasTrans(jni_transa), getCblasDiag(jni_diag), m, n, alpha, jni_a, lda, jni_b, ldb);
  (*env)->ReleaseStringUTFChars(env, diag, jni_diag);
  (*env)->ReleaseStringUTFChars(env, transa, jni_transa);
  (*env)->ReleaseStringUTFChars(env, uplo, jni_uplo);
  (*env)->ReleaseStringUTFChars(env, side, jni_side);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_strmm_1offsets (JNIEnv * env, jobject calling_obj, jstring side, jstring uplo, jstring transa, jstring diag, jint m, jint n, jfloat alpha, jfloatArray a, jint _a_offset, jint lda, jfloatArray b, jint _b_offset, jint ldb) {
  char * jni_side = (char *)(*env)->GetStringUTFChars(env, side, JNI_FALSE);
  char * jni_uplo = (char *)(*env)->GetStringUTFChars(env, uplo, JNI_FALSE);
  char * jni_transa = (char *)(*env)->GetStringUTFChars(env, transa, JNI_FALSE);
  char * jni_diag = (char *)(*env)->GetStringUTFChars(env, diag, JNI_FALSE);
  jfloat * jni_a = NULL;
  if (a != NULL) {
    jni_a = (*env)->GetPrimitiveArrayCritical(env, a, JNI_FALSE);
    check_memory(env, jni_a);
  }
  jfloat * jni_b = NULL;
  if (b != NULL) {
    jni_b = (*env)->GetPrimitiveArrayCritical(env, b, JNI_FALSE);
    check_memory(env, jni_b);
  }
cblas_strmm(CblasColMajor, getCblasSide(jni_side), getCblasUpLo(jni_uplo), getCblasTrans(jni_transa), getCblasDiag(jni_diag), m, n, alpha, jni_a + _a_offset, lda, jni_b + _b_offset, ldb);
  (*env)->ReleaseStringUTFChars(env, diag, jni_diag);
  (*env)->ReleaseStringUTFChars(env, transa, jni_transa);
  (*env)->ReleaseStringUTFChars(env, uplo, jni_uplo);
  (*env)->ReleaseStringUTFChars(env, side, jni_side);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_strmv (JNIEnv * env, jobject calling_obj, jstring uplo, jstring trans, jstring diag, jint n, jfloatArray a, jint lda, jfloatArray x, jint incx) {
  char * jni_uplo = (char *)(*env)->GetStringUTFChars(env, uplo, JNI_FALSE);
  char * jni_trans = (char *)(*env)->GetStringUTFChars(env, trans, JNI_FALSE);
  char * jni_diag = (char *)(*env)->GetStringUTFChars(env, diag, JNI_FALSE);
  jfloat * jni_a = NULL;
  if (a != NULL) {
    jni_a = (*env)->GetPrimitiveArrayCritical(env, a, JNI_FALSE);
    check_memory(env, jni_a);
  }
  jfloat * jni_x = NULL;
  if (x != NULL) {
    jni_x = (*env)->GetPrimitiveArrayCritical(env, x, JNI_FALSE);
    check_memory(env, jni_x);
  }
cblas_strmv(CblasColMajor, getCblasUpLo(jni_uplo), getCblasTrans(jni_trans), getCblasDiag(jni_diag), n, jni_a, lda, jni_x, incx);
  (*env)->ReleaseStringUTFChars(env, diag, jni_diag);
  (*env)->ReleaseStringUTFChars(env, trans, jni_trans);
  (*env)->ReleaseStringUTFChars(env, uplo, jni_uplo);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_strmv_1offsets (JNIEnv * env, jobject calling_obj, jstring uplo, jstring trans, jstring diag, jint n, jfloatArray a, jint _a_offset, jint lda, jfloatArray x, jint _x_offset, jint incx) {
  char * jni_uplo = (char *)(*env)->GetStringUTFChars(env, uplo, JNI_FALSE);
  char * jni_trans = (char *)(*env)->GetStringUTFChars(env, trans, JNI_FALSE);
  char * jni_diag = (char *)(*env)->GetStringUTFChars(env, diag, JNI_FALSE);
  jfloat * jni_a = NULL;
  if (a != NULL) {
    jni_a = (*env)->GetPrimitiveArrayCritical(env, a, JNI_FALSE);
    check_memory(env, jni_a);
  }
  jfloat * jni_x = NULL;
  if (x != NULL) {
    jni_x = (*env)->GetPrimitiveArrayCritical(env, x, JNI_FALSE);
    check_memory(env, jni_x);
  }
cblas_strmv(CblasColMajor, getCblasUpLo(jni_uplo), getCblasTrans(jni_trans), getCblasDiag(jni_diag), n, jni_a + _a_offset, lda, jni_x + _x_offset, incx);
  (*env)->ReleaseStringUTFChars(env, diag, jni_diag);
  (*env)->ReleaseStringUTFChars(env, trans, jni_trans);
  (*env)->ReleaseStringUTFChars(env, uplo, jni_uplo);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_strsm (JNIEnv * env, jobject calling_obj, jstring side, jstring uplo, jstring transa, jstring diag, jint m, jint n, jfloat alpha, jfloatArray a, jint lda, jfloatArray b, jint ldb) {
  char * jni_side = (char *)(*env)->GetStringUTFChars(env, side, JNI_FALSE);
  char * jni_uplo = (char *)(*env)->GetStringUTFChars(env, uplo, JNI_FALSE);
  char * jni_transa = (char *)(*env)->GetStringUTFChars(env, transa, JNI_FALSE);
  char * jni_diag = (char *)(*env)->GetStringUTFChars(env, diag, JNI_FALSE);
  jfloat * jni_a = NULL;
  if (a != NULL) {
    jni_a = (*env)->GetPrimitiveArrayCritical(env, a, JNI_FALSE);
    check_memory(env, jni_a);
  }
  jfloat * jni_b = NULL;
  if (b != NULL) {
    jni_b = (*env)->GetPrimitiveArrayCritical(env, b, JNI_FALSE);
    check_memory(env, jni_b);
  }
cblas_strsm(CblasColMajor, getCblasSide(jni_side), getCblasUpLo(jni_uplo), getCblasTrans(jni_transa), getCblasDiag(jni_diag), m, n, alpha, jni_a, lda, jni_b, ldb);
  (*env)->ReleaseStringUTFChars(env, diag, jni_diag);
  (*env)->ReleaseStringUTFChars(env, transa, jni_transa);
  (*env)->ReleaseStringUTFChars(env, uplo, jni_uplo);
  (*env)->ReleaseStringUTFChars(env, side, jni_side);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_strsm_1offsets (JNIEnv * env, jobject calling_obj, jstring side, jstring uplo, jstring transa, jstring diag, jint m, jint n, jfloat alpha, jfloatArray a, jint _a_offset, jint lda, jfloatArray b, jint _b_offset, jint ldb) {
  char * jni_side = (char *)(*env)->GetStringUTFChars(env, side, JNI_FALSE);
  char * jni_uplo = (char *)(*env)->GetStringUTFChars(env, uplo, JNI_FALSE);
  char * jni_transa = (char *)(*env)->GetStringUTFChars(env, transa, JNI_FALSE);
  char * jni_diag = (char *)(*env)->GetStringUTFChars(env, diag, JNI_FALSE);
  jfloat * jni_a = NULL;
  if (a != NULL) {
    jni_a = (*env)->GetPrimitiveArrayCritical(env, a, JNI_FALSE);
    check_memory(env, jni_a);
  }
  jfloat * jni_b = NULL;
  if (b != NULL) {
    jni_b = (*env)->GetPrimitiveArrayCritical(env, b, JNI_FALSE);
    check_memory(env, jni_b);
  }
cblas_strsm(CblasColMajor, getCblasSide(jni_side), getCblasUpLo(jni_uplo), getCblasTrans(jni_transa), getCblasDiag(jni_diag), m, n, alpha, jni_a + _a_offset, lda, jni_b + _b_offset, ldb);
  (*env)->ReleaseStringUTFChars(env, diag, jni_diag);
  (*env)->ReleaseStringUTFChars(env, transa, jni_transa);
  (*env)->ReleaseStringUTFChars(env, uplo, jni_uplo);
  (*env)->ReleaseStringUTFChars(env, side, jni_side);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_strsv (JNIEnv * env, jobject calling_obj, jstring uplo, jstring trans, jstring diag, jint n, jfloatArray a, jint lda, jfloatArray x, jint incx) {
  char * jni_uplo = (char *)(*env)->GetStringUTFChars(env, uplo, JNI_FALSE);
  char * jni_trans = (char *)(*env)->GetStringUTFChars(env, trans, JNI_FALSE);
  char * jni_diag = (char *)(*env)->GetStringUTFChars(env, diag, JNI_FALSE);
  jfloat * jni_a = NULL;
  if (a != NULL) {
    jni_a = (*env)->GetPrimitiveArrayCritical(env, a, JNI_FALSE);
    check_memory(env, jni_a);
  }
  jfloat * jni_x = NULL;
  if (x != NULL) {
    jni_x = (*env)->GetPrimitiveArrayCritical(env, x, JNI_FALSE);
    check_memory(env, jni_x);
  }
cblas_strsv(CblasColMajor, getCblasUpLo(jni_uplo), getCblasTrans(jni_trans), getCblasDiag(jni_diag), n, jni_a, lda, jni_x, incx);
  (*env)->ReleaseStringUTFChars(env, diag, jni_diag);
  (*env)->ReleaseStringUTFChars(env, trans, jni_trans);
  (*env)->ReleaseStringUTFChars(env, uplo, jni_uplo);
}

JNIEXPORT void JNICALL Java_org_apache_ignite_ml_math_NativeBlasOffHeap_strsv_1offsets (JNIEnv * env, jobject calling_obj, jstring uplo, jstring trans, jstring diag, jint n, jfloatArray a, jint _a_offset, jint lda, jfloatArray x, jint _x_offset, jint incx) {
  char * jni_uplo = (char *)(*env)->GetStringUTFChars(env, uplo, JNI_FALSE);
  char * jni_trans = (char *)(*env)->GetStringUTFChars(env, trans, JNI_FALSE);
  char * jni_diag = (char *)(*env)->GetStringUTFChars(env, diag, JNI_FALSE);
  jfloat * jni_a = NULL;
  if (a != NULL) {
    jni_a = (*env)->GetPrimitiveArrayCritical(env, a, JNI_FALSE);
    check_memory(env, jni_a);
  }
  jfloat * jni_x = NULL;
  if (x != NULL) {
    jni_x = (*env)->GetPrimitiveArrayCritical(env, x, JNI_FALSE);
    check_memory(env, jni_x);
  }
cblas_strsv(CblasColMajor, getCblasUpLo(jni_uplo), getCblasTrans(jni_trans), getCblasDiag(jni_diag), n, jni_a + _a_offset, lda, jni_x + _x_offset, incx);
  (*env)->ReleaseStringUTFChars(env, diag, jni_diag);
  (*env)->ReleaseStringUTFChars(env, trans, jni_trans);
  (*env)->ReleaseStringUTFChars(env, uplo, jni_uplo);
}
