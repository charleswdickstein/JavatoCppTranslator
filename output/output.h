#pragma once
#include "java_lang.h"
#include <string>

using namespace java::lang;

namespace inputs {
  namespace test030 {
  
    struct __A;
    struct __A_VT;
    typedef __rt::Ptr<__A> A;
    
    struct __A { 
      
      __A_VT* __vptr;
      
      __A();

       int32_t i;

      static int32_t get(A);
      static A __init(A, int32_t);
      static A __init(A __this);
      static Class __class();

      static __A_VT __vtable;
    };

    struct __A_VT { 
      Class __is_a;

      void (*__delete) (__A*);
      int32_t (*hashCode) (A);
      bool (*equals) (A, Object);
      Class (*getClass) (A);
      String (*toString) (A);
      int32_t (*get) (A);

      __A_VT()
      : __is_a(__A::__class()),
        __delete(__rt::__delete<__A>),
        hashCode((int32_t (*)(A)) &__Object::hashCode),
        equals((bool (*)(A, Object)) &__Object::equals),
        getClass((Class (*)(A)) &__Object::getClass),
        toString((String (*)(A)) &__Object::toString),
        get(&__A::get) {}
    };

    struct __Test030;
    struct __Test030_VT;
    typedef __rt::Ptr<__Test030> Test030;
    
    struct __Test030 { 
      
      __Test030_VT* __vptr;
      
      __Test030();

      static void main_String_static(String);
      static Test030 __init(Test030 __this);
      static Class __class();

      static __Test030_VT __vtable;
    };

    struct __Test030_VT { 
      Class __is_a;

      void (*__delete) (__Test030*);
      int32_t (*hashCode) (Test030);
      bool (*equals) (Test030, Object);
      Class (*getClass) (Test030);
      String (*toString) (Test030);

      __Test030_VT()
      : __is_a(__Test030::__class()),
        __delete(__rt::__delete<__Test030>),
        hashCode((int32_t (*)(Test030)) &__Object::hashCode),
        equals((bool (*)(Test030, Object)) &__Object::equals),
        getClass((Class (*)(Test030)) &__Object::getClass),
        toString((String (*)(Test030)) &__Object::toString) {}
    };

  }
}
