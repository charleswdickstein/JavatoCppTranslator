#include "output.h"
#include <iostream>

using namespace java::lang;
using namespace std;

using namespace inputs::test030;

int main(int argc, char* argv[]) {
  __rt::Array<String> args = new __rt::__Array<String>(argc - 1);
  for (int32_t i = 1; i < argc; i++) {
    (*args)[i] = __rt::literal(argv[i]);
  }
  __rt::Array<__rt::Array<A>> as =  new __rt::__Array<__rt::Array<A>>(5);
  __rt::Array<__rt::Array<A>> asking =  new __rt::__Array<__rt::Array<A>>(5);
  std::cout << ({Object tmp0 = ({__rt::checkNotNull(as); as->__vptr->getClass(as);}); __rt::checkNotNull(tmp0); __rt::checkNotNull(({__rt::checkNotNull(as); as->__vptr->getClass(as);})); ({__rt::checkNotNull(as); as->__vptr->getClass(as);})->__vptr->toString(({__rt::checkNotNull(as); as->__vptr->getClass(as);}));}) << std::endl;
  return 0;
}
