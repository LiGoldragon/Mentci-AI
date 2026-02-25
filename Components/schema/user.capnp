@0xd60d80e77f9859f5;

struct UserConfig {
  secrets @0 :List(SecretProvider);
}

struct SecretProvider {
  name @0 :Text;
  method @1 :Method;
}

struct Method {
  union {
    gopass @0 :Text; # The path in gopass
    env @1 :Text;    # The environment variable name
    literal @2 :Text; # The actual secret (not recommended for git-tracked files)
  }
}
