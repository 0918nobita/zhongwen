use clap::{Parser, Subcommand};
use qdrant_client::{
    Qdrant,
    qdrant::{CreateCollectionBuilder, Distance, VectorParamsBuilder},
};

#[derive(Debug, Parser)]
struct Args {
    #[command(subcommand)]
    command: Option<Commands>,
}

#[derive(Debug, Subcommand)]
enum Commands {
    CreateCollection,
}

#[tokio::main]
async fn main() -> anyhow::Result<()> {
    let args = Args::parse();

    match args.command {
        Some(Commands::CreateCollection) => {
            create_collection().await?;
        }
        _ => {}
    }

    Ok(())
}

async fn create_collection() -> anyhow::Result<()> {
    let client = Qdrant::from_url("http://localhost:6334").build()?;

    let vector_size = 3;
    let vectors_config = VectorParamsBuilder::new(vector_size, Distance::Euclid);

    let collection_name = "zhongwen";
    let create_collection_request =
        CreateCollectionBuilder::new(collection_name).vectors_config(vectors_config);

    client.create_collection(create_collection_request).await?;

    println!("Collection created successfully");

    Ok(())
}
