#ifndef TAKENGINEJNI_INTEROP_CORE_INTEROP_H_INCLUDED
#define TAKENGINEJNI_INTEROP_CORE_INTEROP_H_INCLUDED

#include <memory>

#include <jni.h>

#include <core/GeoPoint2.h>
#include <core/Layer.h>
#include <core/MapRenderer2.h>
#include <core/MapSceneModel2.h>
#include <core/Projection2.h>

#include "interop/java/JNILocalRef.h"

namespace TAKEngineJNI {
    namespace Interop {
        namespace Core {
            template<class T>
            TAK::Engine::Util::TAKErr Interop_isWrapper(bool *value, JNIEnv &, jobject mlayer) NOTHROWS;

            TAK::Engine::Util::TAKErr Interop_copy(jobject value, JNIEnv *env, const TAK::Engine::Core::GeoPoint2 &p) NOTHROWS;
            TAK::Engine::Util::TAKErr Interop_copy(TAK::Engine::Core::GeoPoint2 *value, JNIEnv *env, jobject p) NOTHROWS;
            jobject Interop_create(JNIEnv *env, const TAK::Engine::Core::GeoPoint2 &p) NOTHROWS;

            jobject Interop_wrap(JNIEnv *env, TAK::Engine::Core::Projection2Ptr &&cproj) NOTHROWS;
            jobject Interop_wrap(JNIEnv *env, const std::shared_ptr<TAK::Engine::Core::Projection2> &cproj) NOTHROWS;

            TAK::Engine::Util::TAKErr Interop_marshal(Java::JNILocalRef &value, JNIEnv &env, const TAK::Engine::Core::MapSceneModel2 &cmodel) NOTHROWS;
            TAK::Engine::Util::TAKErr Interop_marshal(jobject value, JNIEnv &env, const TAK::Engine::Core::MapSceneModel2 &cmodel) NOTHROWS;
            TAK::Engine::Util::TAKErr Interop_marshal(std::shared_ptr<TAK::Engine::Core::MapSceneModel2> &value, JNIEnv &env, jobject mmodel) NOTHROWS;

            // Layer interop
            TAK::Engine::Util::TAKErr Interop_marshal(std::shared_ptr<atakmap::core::Layer> &value, JNIEnv &env, jobject mlayer) NOTHROWS;
            TAK::Engine::Util::TAKErr Interop_marshal(atakmap::core::LayerPtr &value, JNIEnv &env, jobject mlayer) NOTHROWS;
            TAK::Engine::Util::TAKErr Interop_marshal(Java::JNILocalRef &value, JNIEnv &env, const std::shared_ptr<atakmap::core::Layer> &clayer) NOTHROWS;
            TAK::Engine::Util::TAKErr Interop_marshal(Java::JNILocalRef &value, JNIEnv &env, atakmap::core::LayerPtr &&clayer) NOTHROWS;
            TAK::Engine::Util::TAKErr Interop_marshal(Java::JNILocalRef &value, JNIEnv &env, const atakmap::core::Layer &clayer) NOTHROWS;

            template<>
            TAK::Engine::Util::TAKErr Interop_isWrapper<atakmap::core::Layer>(bool *value, JNIEnv &, jobject mlayer) NOTHROWS;
            TAK::Engine::Util::TAKErr Interop_isWrapper(bool *value, JNIEnv &, const atakmap::core::Layer &clayer) NOTHROWS;

            // Layer::VisibilityListener interop
            TAK::Engine::Util::TAKErr Interop_marshal(std::shared_ptr<atakmap::core::Layer::VisibilityListener> &value, JNIEnv &env, jobject mlayer) NOTHROWS;
            TAK::Engine::Util::TAKErr Interop_marshal(std::unique_ptr<atakmap::core::Layer::VisibilityListener, void(*)(const atakmap::core::Layer::VisibilityListener *)> &value, JNIEnv &env, jobject mlayer) NOTHROWS;
            TAK::Engine::Util::TAKErr Interop_marshal(Java::JNILocalRef &value, JNIEnv &env, const std::shared_ptr<atakmap::core::Layer::VisibilityListener> &clayer) NOTHROWS;
            TAK::Engine::Util::TAKErr Interop_marshal(Java::JNILocalRef &value, JNIEnv &env, std::unique_ptr<atakmap::core::Layer::VisibilityListener, void(*)(const atakmap::core::Layer::VisibilityListener *)> &&clayer) NOTHROWS;
            TAK::Engine::Util::TAKErr Interop_marshal(Java::JNILocalRef &value, JNIEnv &env, const atakmap::core::Layer::VisibilityListener &clayer) NOTHROWS;

            template<>
            TAK::Engine::Util::TAKErr Interop_isWrapper<atakmap::core::Layer::VisibilityListener>(bool *value, JNIEnv &, jobject mlayer) NOTHROWS;
            TAK::Engine::Util::TAKErr Interop_isWrapper(bool *value, JNIEnv &, const atakmap::core::Layer::VisibilityListener &clayer) NOTHROWS;

            // RenderContext
            TAK::Engine::Util::TAKErr Interop_marshal(std::shared_ptr<TAK::Engine::Core::RenderContext> &value, JNIEnv &env, jobject mrenderContext) NOTHROWS;
            TAK::Engine::Util::TAKErr Interop_marshal(TAK::Engine::Core::RenderContextPtr &value, JNIEnv &env, jobject mrenderContext) NOTHROWS;
            TAK::Engine::Util::TAKErr Interop_marshal(Java::JNILocalRef &value, JNIEnv &env, const std::shared_ptr<TAK::Engine::Core::RenderContext> &crenderContext) NOTHROWS;
            TAK::Engine::Util::TAKErr Interop_marshal(Java::JNILocalRef &value, JNIEnv &env, TAK::Engine::Core::RenderContextPtr &&crenderContext) NOTHROWS;
            TAK::Engine::Util::TAKErr Interop_marshal(Java::JNILocalRef &value, JNIEnv &env, const TAK::Engine::Core::RenderContext &crenderContext) NOTHROWS;

            template<>
            TAK::Engine::Util::TAKErr Interop_isWrapper<TAK::Engine::Core::RenderContext>(bool *value, JNIEnv &, jobject mrenderContext) NOTHROWS;
            TAK::Engine::Util::TAKErr Interop_isWrapper(bool *value, JNIEnv &, const TAK::Engine::Core::RenderContext &crenderContext) NOTHROWS;

            // IMapRendererEnums
            TAK::Engine::Util::TAKErr Interop_marshal(Java::JNILocalRef &value, JNIEnv &env, const TAK::Engine::Core::MapRenderer::CameraCollision ccameraCollision) NOTHROWS;
            TAK::Engine::Util::TAKErr Interop_marshal(TAK::Engine::Core::MapRenderer::CameraCollision *value, JNIEnv &env, jobject mdisplayOrigin) NOTHROWS;
            TAK::Engine::Util::TAKErr Interop_marshal(Java::JNILocalRef &value, JNIEnv &env, const TAK::Engine::Core::MapRenderer::DisplayMode cdisplayMode) NOTHROWS;
            TAK::Engine::Util::TAKErr Interop_marshal(TAK::Engine::Core::MapRenderer::DisplayMode *value, JNIEnv &env, jobject mdisplayMode) NOTHROWS;
            TAK::Engine::Util::TAKErr Interop_marshal(Java::JNILocalRef &value, JNIEnv &env, const TAK::Engine::Core::MapRenderer::DisplayOrigin cdisplayOrigin) NOTHROWS;
            TAK::Engine::Util::TAKErr Interop_marshal(TAK::Engine::Core::MapRenderer::DisplayOrigin *value, JNIEnv &env, jobject mdisplayOrigin) NOTHROWS;
            TAK::Engine::Util::TAKErr Interop_marshal(Java::JNILocalRef &value, JNIEnv &env, const TAK::Engine::Core::MapRenderer::InverseMode cinverseMode) NOTHROWS;
            TAK::Engine::Util::TAKErr Interop_marshal(TAK::Engine::Core::MapRenderer::InverseMode *value, JNIEnv &env, jobject minverseMode) NOTHROWS;
            TAK::Engine::Util::TAKErr Interop_marshal(Java::JNILocalRef &value, JNIEnv &env, const TAK::Engine::Core::MapRenderer::InverseResult cinverseResult) NOTHROWS;
            TAK::Engine::Util::TAKErr Interop_marshal(TAK::Engine::Core::MapRenderer::InverseResult *value, JNIEnv &env, jobject minverseResult) NOTHROWS;
        }
    }
}
#endif
