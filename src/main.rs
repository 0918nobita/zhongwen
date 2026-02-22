use anyhow::Context;
use clap::{Parser, Subcommand};
use qdrant_client::{
    Payload, Qdrant,
    qdrant::{
        CreateCollectionBuilder, Distance, PointStruct, UpsertPointsBuilder, VectorParamsBuilder,
    },
};

#[derive(Debug, Parser)]
struct Args {
    #[command(subcommand)]
    command: Option<Commands>,
}

#[derive(Debug, Subcommand)]
enum Commands {
    UpsertPoint,
    CreateCollection,
}

#[tokio::main]
async fn main() -> anyhow::Result<()> {
    let args = Args::parse();

    match args.command {
        Some(Commands::CreateCollection) => {
            create_collection().await?;
        }
        Some(Commands::UpsertPoint) => {
            upsert_point().await?;
        }
        None => {}
    }

    Ok(())
}

async fn get_client() -> anyhow::Result<Qdrant> {
    Qdrant::from_url("http://localhost:6334")
        .build()
        .context("Failed to create Qdrant client")
}

async fn create_collection() -> anyhow::Result<()> {
    let client = get_client().await?;

    let vector_size = 3;
    let vectors_config = VectorParamsBuilder::new(vector_size, Distance::Euclid);

    let collection_name = "zhongwen";
    let create_collection_request =
        CreateCollectionBuilder::new(collection_name).vectors_config(vectors_config);

    client.create_collection(create_collection_request).await?;

    println!("Collection created successfully");
    Ok(())
}

async fn upsert_point() -> anyhow::Result<()> {
    let client = get_client().await?;

    let collection_name = "zhongwen";

    let payload: Payload = serde_json::json!({
        "example": "在挫折面前，他从不灰心。",
    })
    .try_into()?;

    let points = vec![PointStruct::new(0, vec![0.1, 0.2, 0.3], payload)];

    let upsert_points_request = UpsertPointsBuilder::new(collection_name, points);
    client.upsert_points(upsert_points_request).await?;

    println!("Point upserted successfully");
    Ok(())
}
