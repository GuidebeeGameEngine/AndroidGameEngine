/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_guidebee_game_physics_box2d_Fixture */

#ifndef _Included_com_guidebee_game_physics_box2d_Fixture
#define _Included_com_guidebee_game_physics_box2d_Fixture
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_guidebee_game_physics_box2d_Fixture
 * Method:    jniGetType
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_com_guidebee_game_physics_Fixture_jniGetType
  (JNIEnv *, jobject, jlong);

/*
 * Class:     com_guidebee_game_physics_box2d_Fixture
 * Method:    jniGetShape
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_com_guidebee_game_physics_Fixture_jniGetShape
  (JNIEnv *, jobject, jlong);

/*
 * Class:     com_guidebee_game_physics_box2d_Fixture
 * Method:    jniSetSensor
 * Signature: (JZ)V
 */
JNIEXPORT void JNICALL Java_com_guidebee_game_physics_Fixture_jniSetSensor
  (JNIEnv *, jobject, jlong, jboolean);

/*
 * Class:     com_guidebee_game_physics_box2d_Fixture
 * Method:    jniIsSensor
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL Java_com_guidebee_game_physics_Fixture_jniIsSensor
  (JNIEnv *, jobject, jlong);

/*
 * Class:     com_guidebee_game_physics_box2d_Fixture
 * Method:    jniSetFilterData
 * Signature: (JSSS)V
 */
JNIEXPORT void JNICALL Java_com_guidebee_game_physics_Fixture_jniSetFilterData
  (JNIEnv *, jobject, jlong, jshort, jshort, jshort);

/*
 * Class:     com_guidebee_game_physics_box2d_Fixture
 * Method:    jniGetFilterData
 * Signature: (J[S)V
 */
JNIEXPORT void JNICALL Java_com_guidebee_game_physics_Fixture_jniGetFilterData
  (JNIEnv *, jobject, jlong, jshortArray);

/*
 * Class:     com_guidebee_game_physics_box2d_Fixture
 * Method:    jniRefilter
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_guidebee_game_physics_Fixture_jniRefilter
  (JNIEnv *, jobject, jlong);

/*
 * Class:     com_guidebee_game_physics_box2d_Fixture
 * Method:    jniTestPoint
 * Signature: (JFF)Z
 */
JNIEXPORT jboolean JNICALL Java_com_guidebee_game_physics_Fixture_jniTestPoint
  (JNIEnv *, jobject, jlong, jfloat, jfloat);

/*
 * Class:     com_guidebee_game_physics_box2d_Fixture
 * Method:    jniSetDensity
 * Signature: (JF)V
 */
JNIEXPORT void JNICALL Java_com_guidebee_game_physics_Fixture_jniSetDensity
  (JNIEnv *, jobject, jlong, jfloat);

/*
 * Class:     com_guidebee_game_physics_box2d_Fixture
 * Method:    jniGetDensity
 * Signature: (J)F
 */
JNIEXPORT jfloat JNICALL Java_com_guidebee_game_physics_Fixture_jniGetDensity
  (JNIEnv *, jobject, jlong);

/*
 * Class:     com_guidebee_game_physics_box2d_Fixture
 * Method:    jniGetFriction
 * Signature: (J)F
 */
JNIEXPORT jfloat JNICALL Java_com_guidebee_game_physics_Fixture_jniGetFriction
  (JNIEnv *, jobject, jlong);

/*
 * Class:     com_guidebee_game_physics_box2d_Fixture
 * Method:    jniSetFriction
 * Signature: (JF)V
 */
JNIEXPORT void JNICALL Java_com_guidebee_game_physics_Fixture_jniSetFriction
  (JNIEnv *, jobject, jlong, jfloat);

/*
 * Class:     com_guidebee_game_physics_box2d_Fixture
 * Method:    jniGetRestitution
 * Signature: (J)F
 */
JNIEXPORT jfloat JNICALL Java_com_guidebee_game_physics_Fixture_jniGetRestitution
  (JNIEnv *, jobject, jlong);

/*
 * Class:     com_guidebee_game_physics_box2d_Fixture
 * Method:    jniSetRestitution
 * Signature: (JF)V
 */
JNIEXPORT void JNICALL Java_com_guidebee_game_physics_Fixture_jniSetRestitution
  (JNIEnv *, jobject, jlong, jfloat);

#ifdef __cplusplus
}
#endif
#endif
