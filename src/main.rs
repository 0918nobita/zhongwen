use anyhow::Context;
use clap::{Parser, Subcommand};
use qdrant_client::{
    Payload, Qdrant,
    qdrant::{
        CreateCollectionBuilder, DeletePointsBuilder, Distance, PointId, PointStruct,
        PointsIdsList, UpsertPointsBuilder, VectorParamsBuilder,
    },
};

#[derive(Debug, Parser)]
struct Args {
    #[command(subcommand)]
    command: Option<Commands>,
}

#[derive(Debug, Subcommand)]
enum Commands {
    CreateCollection,
    DeletePoint {
        /// The ID of the Qdrant point to delete
        id: String,
    },
    UpsertPoint,
}

const COLLECTION_NAME: &str = "zhongwen";

#[tokio::main]
async fn main() -> anyhow::Result<()> {
    let args = Args::parse();

    match args.command {
        Some(Commands::CreateCollection) => {
            create_collection().await?;
        }
        Some(Commands::DeletePoint { id }) => {
            delete_point(id).await?;
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

    let create_collection_request =
        CreateCollectionBuilder::new(COLLECTION_NAME).vectors_config(vectors_config);

    client.create_collection(create_collection_request).await?;

    println!("Collection created successfully");
    Ok(())
}

async fn upsert_point() -> anyhow::Result<()> {
    let client = get_client().await?;

    let payload: Payload = serde_json::json!({
        "example": "在挫折面前，他从不灰心。",
    })
    .try_into()?;

    let points = vec![PointStruct::new(0, vec![0.1, 0.2, 0.3], payload)];

    let upsert_points_request = UpsertPointsBuilder::new(COLLECTION_NAME, points);
    client.upsert_points(upsert_points_request).await?;

    println!("Point upserted successfully");
    Ok(())
}

async fn delete_point(id: String) -> anyhow::Result<()> {
    let client = get_client().await?;

    let id = match id.parse::<u64>() {
        Ok(id) => PointId::from(id),
        Err(_) => PointId::from(id),
    };

    let delete_point_request = DeletePointsBuilder::new(COLLECTION_NAME)
        .points(PointsIdsList { ids: vec![id] })
        .wait(true);

    client.delete_points(delete_point_request).await?;

    println!("Point deleted successfully");
    Ok(())
}
