fn main() {
    capnpc::CompilerCommand::new()
        .src_prefix("../schema")
        .file("../schema/mentci.capnp")
        .file("../schema/atom_filesystem.capnp")
        .run()
        .expect("schema compiler command");
}
