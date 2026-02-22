fn main() {
    capnpc::CompilerCommand::new()
        .src_prefix("schema")
        .file("schema/atom_filesystem.capnp")
        .file("schema/mentci.capnp")
        .run()
        .expect("schema compilation failed");
}
