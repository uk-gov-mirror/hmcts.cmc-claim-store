terraform {
  backend "azurerm" {}

    required_providers {
      azurerm = {
        source  = "hashicorp/azurerm"
        version = "=2.49.0"
      }
      random = {
        source = "hashicorp/random"
      }
    }
}

provider "azurerm" {
  alias = "send-grid"
  subscription_id = "1c4f0704-a29e-403d-b719-b90c34ef14c9"
  features {}
}

provider "azurerm" {
  alias = "infra"
  subscription_id = "1c4f0704-a29e-403d-b719-b90c34ef14c9"
  features {}
}